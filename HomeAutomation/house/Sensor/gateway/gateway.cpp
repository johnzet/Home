/*
 * gateway.cpp
 *
 * Created: 1/7/2012 12:18:20 PM
 *  Author: johnz
 */

#define TWI_FREQ 100000UL
//extern "C" void __cxa_pure_virtual(void);

#include <Arduino.h>
#include <avr/wdt.h>
#include "HardwareSerial.h"
#include "EEPROM.h"
#include "xbee.h"
#include "wire.h"
#include "sht2x.h"
#include "mpl115a2.h"
#include "RelayBrd.h"

// Digital pins
#define HVAC_ADDRESS_SET_PIN 6
#define SPRINKLER_ADDRESS_SET_PIN 7
#define LED_PIN 13
#define LOOP_IND_PIN 12
#define HANG_SIM_PIN 11

// Analog pins

// Constants
#define HVAC_RELAY_ADDRESS 0x72
#define SPRINKLER_RELAY_ADDRESS 0x74
#define XBEE_RECEIVE_TIMEOUT 500
/*
* MPL115A2 i2c address = 0x60
* SHT2x I2c address = 0x40
* Relay Board i2c address = 0x38 - 0x3F
* NOTE:  All addresses above are the upper 7 bits ****
*/

// Functions
void sendRequest(uint8_t *msg, int length);
void sendValueMessageToXBee(float temperature, float humidity, float pressure);
void sendRelayStateMessageToXBee(void);
void sendErrorCountsToXbee(void);
//bool receiveStatusResponse();
bool receiveServerResponse(void);
void pollForXbeeMessage(void);
void clearLcd(void);
void initLcd(void);
void writeLcd(const char *str);
void parseServerResponse(ZBRxResponse response);
void checkForCommandOverride(void);
void handleServerDisconnect(void);
void flash_led(uint8_t count);
void delaySeconds(uint8_t seconds);

// Globals
XBee xbee = XBee();
XBeeAddress64 addr64 = XBeeAddress64(0x00000000/*0x0013a200*/, 0x00000000 /*0x407c4548*/);
uint8_t loopCounter = 0;
uint8_t sensorCommErrors = 0;
uint8_t xbeeCommErrors = 0;
uint8_t xbeeTimeoutErrors = 0;
uint8_t missedResponseCount = 0;
char buffer[80];
char buffer2[80];
uint8_t wdtResetCount = 0;
bool wdtResetFlag = false;

void setup() { 
	// disable the watchdog timer
	uint8_t mcusr = GPIOR0;  // The bootloader saved this away
	wdtResetFlag = mcusr & 1<<WDRF;
	MCUSR = 0;  //  probably not necessary if we ran from the bootloader

	// enable watchdog timer
	wdt_reset();
	wdt_enable(WDTO_8S);
	wdt_reset();

	// Update the watchdog reset count
	wdtResetCount = EEPROM.read(0);
	if (wdtResetFlag) {
    	// We were reset by the watchdog timer
    	wdtResetCount++;
    	EEPROM.write(0, wdtResetCount);
	}

	// Init pins
	pinMode(LED_PIN, OUTPUT);
	pinMode(LOOP_IND_PIN, OUTPUT);
	pinMode(HANG_SIM_PIN, INPUT);
	digitalWrite(LED_PIN, LOW);
	digitalWrite(LOOP_IND_PIN, LOW);
	digitalWrite(HANG_SIM_PIN, HIGH);  // enable pull-up
	
	pinMode(HVAC_ADDRESS_SET_PIN, INPUT);
	digitalWrite(HVAC_ADDRESS_SET_PIN, HIGH);  // enable the pull-up - Should have an external pullup too.
	
	pinMode(SPRINKLER_ADDRESS_SET_PIN, INPUT);
	digitalWrite(SPRINKLER_ADDRESS_SET_PIN, HIGH);  // enable the pull-up - Should have an external pullup too.

	// Lcd init
	delay(2000);
	initLcd();
	writeLcd("      Zehetner| |      Gateway");
	delay(3000);

	// Xbee and I2C init
	Wire.begin();
    Serial1.begin(38400);
    xbee.setSerial(Serial1);
	xbee.begin(38400);

	// Barometer init
	MPL115A2.begin();
	//MPL115A2.shutdown();
}

void loop()
{
	// enable watchdog timer
	wdt_enable(WDTO_8S);
	wdt_reset();
    digitalWrite(LOOP_IND_PIN, HIGH);
    
    while (digitalRead(HANG_SIM_PIN) == LOW) {
        digitalWrite(LOOP_IND_PIN, LOW);
        digitalWrite(LOOP_IND_PIN, HIGH);
    }  

	loopCounter++;
	
	checkForCommandOverride();

    // Receive the data from the previous sample so that we don't need
	// to wait for it to arrive.
	if (!receiveServerResponse()) {
		missedResponseCount++;
	} else {
		missedResponseCount = 0;
	}

	if (missedResponseCount > 200) {
		handleServerDisconnect();
	}
	
	if (missedResponseCount > 15 && missedResponseCount % 10 == 0) {
		xbeeTimeoutErrors++;
	}

    digitalWrite(LOOP_IND_PIN, LOW);

	if (loopCounter > 10) {
		MPL115A2.ReadSensor();
		//MPL115A2.shutdown();
		float temperature = SHT2x.GetTemperature();
		float humidity = SHT2x.GetHumidity();
		float pressure = MPL115A2.GetPressure();

		if (temperature > -45.0 && humidity > -1.0 && pressure > 0.0) {
			sendValueMessageToXBee(temperature, humidity, pressure);
		} else {
			sensorCommErrors++;
		}			
		
		sendRelayStateMessageToXBee();
		sendErrorCountsToXbee();
		
		loopCounter = 0;
	}

}

void sendValueMessageToXBee(float temperature, float humidity, float pressure) {
    sprintf(buffer, "SensorData TEMPERATURE_C %i.%02i |HUMIDITY %i.%02i |BAROMETER_KPA %i.%02i",
	    (int)temperature,  (int)(fabs(temperature - ((int)temperature)) * 100),
	    (int)humidity,  (int)(fabs(humidity - ((int)humidity)) * 100),
	    (int)pressure,  (int)(fabs(pressure - ((int)pressure)) * 100)
	);

	sendRequest((uint8_t *)buffer, strlen(buffer));
}

void sendErrorCountsToXbee() {
    sprintf(buffer, "ErrorCounts SENSOR %i COMM %i TIMEOUT %i WDT %i", 
		sensorCommErrors, xbeeCommErrors, xbeeTimeoutErrors, wdtResetCount);
	sendRequest((uint8_t *)buffer, strlen(buffer));
}

void sendRelayStateMessageToXBee() {
	uint8_t hvacState = RelayBrd.readState(HVAC_RELAY_ADDRESS);
	uint8_t sprinklerState = RelayBrd.readState(SPRINKLER_RELAY_ADDRESS);
	
	sprintf(buffer, "HvacRelayState %i", hvacState);
	sendRequest((uint8_t *)buffer, strlen(buffer));
	
	sprintf(buffer, "SprinklerRelayState %i", sprinklerState);
	sendRequest((uint8_t *)buffer, strlen(buffer));
}

void sendRequest(uint8_t *msg, int length) {


    ZBTxRequest zbTx = ZBTxRequest(addr64, msg, length);

	//zbTx.setFrameId(0);  // disable status responses
    xbee.send(zbTx);

    //receiveStatusResponse();
}

bool receiveServerResponse() {
	bool responded = false;
	bool timeout = false;

	do {
    	xbee.readPacket(XBEE_RECEIVE_TIMEOUT);
        if (xbee.getResponse().isAvailable()) {
		
            if (xbee.getResponse().getApiId() ==  ZB_RX_RESPONSE) {
				flash_led(1);
			    ZBRxResponse response = ZBRxResponse();
			    xbee.getResponse().getZBRxResponse(response);
			    parseServerResponse(response);
			    responded = true;
            } else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
				ModemStatusResponse msr = ModemStatusResponse();
				xbee.getResponse().getModemStatusResponse(msr);
				sprintf(buffer, "Modem Status| Response 0x%x", msr.getStatus());
				writeLcd(buffer);
				delay(300);
    	    } else if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
			    // normal - do nothing
			} else {
				// There is something else waiting - get it and ignore it.
				xbee.getResponse().getFrameData();
				sprintf(buffer, "Unknown Pkt| Type 0x%x", xbee.getResponse().getApiId());
				writeLcd(buffer);
				delay(300);
			}
		} else if (xbee.getResponse().isError()) {
			xbeeCommErrors++;
	        uint8_t errorCode = xbee.getResponse().getErrorCode();
		    sprintf(buffer, "Receive Error|Error code 0x%x", errorCode);
	        writeLcd(buffer);
			delay(300);
            if (errorCode == UNEXPECTED_START_BYTE) {
                // The xbee API doesn't handle multiple packets in one response 
                // which can  cause this error.  Try to flush.
                do {} while (receiveServerResponse());
            }                
		} else {
			timeout = true;
		}		
	} while (!timeout);	
    //if (!responded) {
	    //writeLcd("Receive Timeout|Is the Server Down?");
	//}
	return responded;		
}

void parseServerResponse(ZBRxResponse response) {
	char* data = (char *)response.getData();
	if (data == NULL) return;

	char* command = strtok(data, " ");
	char* argument = strtok(NULL, "");

	if (strcmp(command, "LcdText") == 0) {
        writeLcd(argument);
	} else if (strcmp(command, "HvacRelayState") == 0) {
		RelayBrd.setState(HVAC_RELAY_ADDRESS, atoi(argument) & 0xFF);
	} else if (strcmp(command, "SprinklerRelayState") == 0) {
		RelayBrd.setState(SPRINKLER_RELAY_ADDRESS, atoi(argument) & 0xFF);
	}
}

//bool receiveStatusResponse() {
//
    //// after sending a tx request, we expect a status response
    //// wait up to half second for the status response
    //if (xbee.readPacket(500)) {
        //// got a response!
//
        //// may be a znet tx status
    	//if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
            //ZBTxStatusResponse txStatus = ZBTxStatusResponse();
    	    //xbee.getResponse().getZBTxStatusResponse(txStatus);
//
    	   //// get the delivery status, the fifth byte
           //if (txStatus.getDeliveryStatus() == SUCCESS) {
               //return true;
           //} else {
			   	//writeLcd("Bad Status");
		   //}
        //}
		//writeLcd("Bad Packet");
    //}
	//writeLcd("Transmit Timeout|Is the Server Down?");
//
	//return false;
//}

void initLcd(void) {
    Serial2.begin(9600);

	// backlight brightness
	Serial2.write(0x7C);
	Serial2.write(0x8C);  //Brightness 0x80 - 0x9D
	delay(10);

    // 20 columns
	Serial2.write(0x7C);
	Serial2.write(0x3);
	delay(10);

    // 4 lines
	Serial2.write(0x7C);
	Serial2.write(0x5);
	delay(10);
}

void clearLcd(void) {
	Serial2.write(0xFE);
	Serial2.write(0x01);
	delay(10);
}

void _lcdWriteString(char *text, uint8_t lineNumber /* 1-based*/)
{
	// Line 1 = Address 0x00 through 0x13
    // Line 2 = Address 0x40 through 0x53
    // Line 3 = Address 0x14 through 0x27
    // Line 4 = Address 0x54 through 0x67

	uint16_t address;
	switch(lineNumber) {
		case(1):
		    address = 0x00;
			break;
		case(2):
		    address = 0x40;
			break;
		case(3):
		    address = 0x14;
			break;
		case(4):
		    address = 0x54;
			break;
		default:
		    return;
			break;
	}

	// set cursor position
	Serial2.write(0xFE);
	Serial2.write(0x80 + address);

    Serial2.write(text);
}


void writeLcd(const char *str) {

    clearLcd();
	
	strcpy(buffer2, str);

    uint8_t lineNumber = 1;
    char *pch = strtok (buffer2,"|");
    while (pch != NULL) {
    	_lcdWriteString(pch, lineNumber++);
		delay(50);
        pch = strtok (NULL, "|");
    }

	delay(200);
}


void checkForCommandOverride() {
	if (digitalRead(HVAC_ADDRESS_SET_PIN) == LOW) {
		RelayBrd.changeAddress(HVAC_RELAY_ADDRESS);
		writeLcd("Set HVAC relay|board address to|0x72");
		delaySeconds(10);
	}
	if (digitalRead(SPRINKLER_ADDRESS_SET_PIN) == LOW) {
		RelayBrd.changeAddress(SPRINKLER_RELAY_ADDRESS);
		writeLcd("Set Sprinkler relay|board address to|0x74");
		delaySeconds(10);
	}
}

void handleServerDisconnect() {
    RelayBrd.setState(HVAC_RELAY_ADDRESS, 0);
    RelayBrd.setState(SPRINKLER_RELAY_ADDRESS, 0);
	writeLcd("Lost Server|Connection.|ALL SYSTEMS OFF");
}

// -------------------------------------------------------------------------------
//		flash_led
//			Blink the led on/off <count> times
// -------------------------------------------------------------------------------
void flash_led(uint8_t count) {

	uint8_t i;

	for (i = 0; i < count; i++)
	{
		digitalWrite(LED_PIN, HIGH);
		delay(100);
		digitalWrite(LED_PIN, LOW);
		delay(100);
	}
}

void delaySeconds(uint8_t seconds) {
	for (uint8_t i=0; i<seconds; i++) {
		wdt_reset();
		delay(1000);
	}
	wdt_reset();
}
