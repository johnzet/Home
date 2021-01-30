/*
 * thermostat.cpp
 *
 * Created: 12/10/2011 7:47:25 PM
 *  Author: johnz
 */

#define TWI_FREQ 100000UL

#include <Arduino.h>
#include <stdio.h>
#include <stdint.h>
#include "Wire.h"
#include "XBee.h"
#include "SensorUtils.h"
#include "SHT2x.h"
#include "Lcd.h"
#include "EEPROM.h"
#include <avr/sleep.h>
#include <avr/wdt.h>

// Digital pins
#define INTERRUPT_PIN 2 /* INT0 */
#define CTS_PIN 3 /* INT1 */
#define SPKR_PIN 6 
#define LCD_POWER_PIN 7
#define BAT_SENSE_EN_PIN 8
#define LED_PIN 13

// Analog pins
#define BATSENSE_PIN 0
#define BACKLIGHT_PIN 1
#define UP_PIN 2
#define DOWN_PIN 3

// Constants
#define XBEE_RECEIVE_TIMEOUT 1900
//#define XBEE_STATUS_TIMEOUT 2000
#define DISPLAY_DEFAULT_COUNTER 2

// Functions
void sendValueMessageToXBee(float temperature, float humidity, uint16_t batLevel);
void sendErrorCountsToXbee(void);
void sendTempChangeRequestToXbee(int change);
//bool receiveStatusResponse();
bool receiveLcdContentResponse(void);
void pollForXbeeMessage(void);
void writeLcd(const char *buffer);
char* getLcdTextFromResponse(ZBRxResponse zBRxResponse);
void flash_led(uint8_t count);
void wakeIrqHandler(void);
void buttonIrqHandler(void);
uint16_t getBatteryLevel(void);
void beep(boolean longBeep);
uint16_t getBssEnd(void);
uint16_t getStackEnd(void);

// Globals
XBee xbee = XBee();
SensorUtils utils = SensorUtils(&xbee);
XBeeAddress64 addr64 = XBeeAddress64(0x00000000/*0x0013a200*/, 0x00000000 /*0x407c4548*/);
ZBRxResponse zbRxResponse = ZBRxResponse();
//ZBTxStatusResponse txStatus = ZBTxStatusResponse();
uint8_t sensorCommErrors = 0;
uint8_t xbeeCommErrors = 0;
uint8_t xbeeTimeoutErrors = 0;
bool backLightButtonPressed = false;
bool upButtonPressed = false;
bool downButtonPressed = false;
int displayCounter = DISPLAY_DEFAULT_COUNTER;
char buffer[80];
int tempChange = 0;
uint8_t wdtResetCount = 0;
bool wdtResetFlag = false;
uint8_t rssi = 0;
bool stillWaitingForResonse;



void setup() {
	// disable the watchdog timer
	uint8_t mcusr = GPIOR0;  // The bootloader saved this away
	wdtResetFlag = mcusr & 1<<WDRF;
	MCUSR = 0;  //  probably not necessary if we ran from the bootloader
	wdt_disable();
	
	// Init pins
	pinMode(CTS_PIN, INPUT);
	digitalWrite(CTS_PIN, LOW);  // disable the pull-up resistor
	
	pinMode(INTERRUPT_PIN, INPUT);
	digitalWrite(INTERRUPT_PIN, HIGH); // enable the pull-up resistor
	
	digitalWrite(SPKR_PIN, LOW);
	pinMode(SPKR_PIN, OUTPUT);

	digitalWrite(LCD_POWER_PIN, HIGH);
	pinMode(LCD_POWER_PIN, OUTPUT);  // Active low

	digitalWrite(BAT_SENSE_EN_PIN, LOW);
	pinMode(BAT_SENSE_EN_PIN, OUTPUT);

	digitalWrite(LED_PIN, LOW);
	pinMode(LED_PIN, OUTPUT);
	
	analogReference(INTERNAL);  //Use internal 1.1V ADC reference
	
	// Lcd init
	digitalWrite(LCD_POWER_PIN, LOW);
	// wait for the LCD to power up
	delay(500);
    Lcd.displayOn();

	Lcd.writeLcd("      Zehetner| |    Thermostat 1");

	// Xbee and I2C init
	Wire.begin();
	xbee.begin(38400);

	// Update the watchdog reset count
	wdtResetCount = EEPROM.read(0);
	if (wdtResetFlag) {
		// We were reset by the watchdog timer
		wdtResetCount++;
		EEPROM.write(0, wdtResetCount);
	}
}

void loop(void)
{
	detachInterrupt(INT0);
	detachInterrupt(INT1);

	// enable watchdog timer
	wdt_enable(WDTO_8S);
	wdt_reset();
	
	
	if (backLightButtonPressed | upButtonPressed | downButtonPressed) {
		displayCounter = DISPLAY_DEFAULT_COUNTER;
	}
	if (upButtonPressed) {
		tempChange++;
	} else if (downButtonPressed) {
		tempChange--;
	}
	if (upButtonPressed | downButtonPressed) {
		sprintf(buffer, " | Temperature %s %i", ((tempChange >= 0)? "UP" : "DOWN"), abs(tempChange));
		Lcd.writeLcd(buffer);
	}		

	if (displayCounter > 0 && !Lcd.isDisplayOn()) {
		digitalWrite(LCD_POWER_PIN, LOW);
		// wait for the LCD to power up
		delay(100);
		Lcd.displayOn();
	} else if (displayCounter == 0) {
		Lcd.displayOff();
		digitalWrite(LCD_POWER_PIN, HIGH); // LCD off
	} 
	
	if (displayCounter >= 0) {
		displayCounter--;
	}
	wdt_reset();

	// If we were interrupted, the Xbee radio may not be awake yet.  Proceed only if it is awake.
	if (!digitalRead(CTS_PIN)) {

		// Note: When the Xbee wakes from cyclic sleep, it will not poll its 
		// parent for data until it sends something first.
		sendErrorCountsToXbee();
		wdt_reset();
		
		// Check if there is a late LCD content response and throw it away 
		xbee.readPacket(200);
		if (xbee.getResponse().isAvailable()) stillWaitingForResonse = false;
	
		if (tempChange != 0) {
			sendTempChangeRequestToXbee(tempChange);
			wdt_reset();
			tempChange = 0;
			delay(5);
		}

		uint16_t batLevel = getBatteryLevel();
		float temperature = SHT2x.GetTemperature();
		float humidity = SHT2x.GetHumidity();
		wdt_reset();
		if (temperature > -45.0 && humidity > -1.0) {
			sendValueMessageToXBee(temperature, humidity, batLevel);
			wdt_reset();
		} else {
			sensorCommErrors++;
		}

		// add a delay between transmit and receive
		delay(5);
    
		bool responded = receiveLcdContentResponse();
		wdt_reset();
		
		// put the Xbee to sleep now rather than wait for the sleep timer to expire
		if (responded) {
			delay(5);
			rssi = utils.getRSSI();
			wdt_reset();
			
			delay(5);

			utils.sleepXbeeImmediately();
			delay(5);  // Wait for the SI command to go out.
		}	
	}	

	backLightButtonPressed = false;
	upButtonPressed = false;
	downButtonPressed = false;

	// disable the watchdog timer
	wdt_reset();
	wdt_disable();

	attachInterrupt(INT0, buttonIrqHandler, FALLING);
	attachInterrupt(INT1, wakeIrqHandler, FALLING);
	
	set_sleep_mode(SLEEP_MODE_PWR_DOWN);
	sleep_enable();
	sleep_cpu();
}

void wakeIrqHandler(void) {
	// wake
	sleep_disable();
	// return to the very bottom of loop()
}	

void buttonIrqHandler(void) {
	if (!backLightButtonPressed) {
		backLightButtonPressed = analogRead(BACKLIGHT_PIN) < 500;
	}		
	upButtonPressed = analogRead(UP_PIN) < 500;
	downButtonPressed = analogRead(DOWN_PIN) < 500;

	if (upButtonPressed | downButtonPressed) {
		detachInterrupt(INT0);
	}
			
	if (backLightButtonPressed | upButtonPressed | downButtonPressed) {
		sleep_disable();
		beep(false /* short beep */);
	}		
	// return to the very bottom of loop()
}

void sendValueMessageToXBee(float temperature, float humidity, uint16_t batLevel) {
	double batFullLevel = 4.175;  // Measured - Full voltage is nominally 4.2V
	double batDeadLevel = 3.5;  // Estimate from discharge curve
	double batVoltage = (batLevel / 224.4);  // Calculated from measurements
	if (batVoltage > batFullLevel) batVoltage = batFullLevel;
	if (batVoltage < batDeadLevel) batVoltage = batDeadLevel;
	int batPercent = (int)( ((batVoltage - batDeadLevel) / (batFullLevel - batDeadLevel)) * 100.0);
    sprintf(buffer, "SensorData TEMPERATURE_C %i.%02i |HUMIDITY %i.%02i |BAT_PCT %i",
	    (int)temperature,  (int)(fabs(temperature - ((int)temperature)) * 100),
	    (int)humidity,  (int)(fabs(humidity - ((int)humidity)) * 100),
	    batPercent
	);

	utils.sendRequest(&addr64, (uint8_t *)buffer, strlen(buffer));
}

void sendErrorCountsToXbee() {
	sprintf(buffer, "ErrorCounts SENSOR %i COMM %i TIMEOUT %i WDT %i RSSI %i",
		sensorCommErrors, xbeeCommErrors, xbeeTimeoutErrors, wdtResetCount, -rssi);
	utils.sendRequest(&addr64, (uint8_t *)buffer, strlen(buffer));
}

void sendTempChangeRequestToXbee(int change) {
	sprintf(buffer, "TemperatureChange %i", change);
	utils.sendRequest(&addr64, (uint8_t *)buffer, strlen(buffer));
}

char* getLcdTextFromResponse(ZBRxResponse zBRxResponse) {
	strncpy(buffer, (char*)zBRxResponse.getData(), zBRxResponse.getDataLength());
	buffer[zBRxResponse.getDataLength()] = 0;
	if (strlen(buffer) < 8) {
		return NULL;
	}
	if (strncmp("LcdTextBeep ", buffer, 12) == 0) {
		beep(true);
		return buffer + 12;
	} else if (strncmp("LcdText ", buffer, 8) == 0) {
		return buffer + 8;
	}
	return NULL;
}

bool receiveLcdContentResponse() {
	int timeout = XBEE_RECEIVE_TIMEOUT;
	bool responded = false;
	bool timedOut = false;
	do {
		xbee.readPacket(timeout);
		wdt_reset();
		if (xbee.getResponse().isAvailable()) {
			if (xbee.getResponse().getApiId() ==  ZB_RX_RESPONSE) {
				xbee.getResponse().getZBRxResponse(zbRxResponse);
				char *lcdText = getLcdTextFromResponse(zbRxResponse);
				if (lcdText != NULL) {
					Lcd.writeLcd(lcdText);
				}
				responded = true;
				timeout = 100;
			} else if (xbee.getResponse().getApiId() == MODEM_STATUS_RESPONSE) {
				ModemStatusResponse msr = ModemStatusResponse();
				xbee.getResponse().getModemStatusResponse(msr);
				sprintf(buffer, "Modem Status|Response 0x%x", msr.getStatus());
				Lcd.writeLcd(buffer);
    			delay(300);
		    } else if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
				// normal - do nothing
		    } else if (xbee.getResponse().getApiId() == AT_RESPONSE) {
			    // normal - do nothing
			} else {
				// There is something else waiting - get it and ignore it.
				xbee.getResponse().getFrameData();
				sprintf(buffer, "Unknown Pkt|Type 0x%x", xbee.getResponse().getApiId());
				Lcd.writeLcd(buffer);
				delay(300);
			}
		} else if (xbee.getResponse().isError()) {
			xbeeCommErrors++;
			uint8_t errorCode = xbee.getResponse().getErrorCode();
			sprintf(buffer, "Receive Error|Error code 0x%x", errorCode);
			Lcd.writeLcd(buffer);
			zbRxResponse.reset();
		} else {
			timedOut = true;
		}
	} while (!timedOut);
	if (!responded) {
		if (!stillWaitingForResonse) {
			stillWaitingForResonse = true;
		} else {
			xbeeTimeoutErrors++;
			Lcd.writeLcd("Receive Timeout|Is the Server Down?");
			zbRxResponse.reset();
		}			
	}
	return responded;
}

//bool receiveStatusResponse() {
	//if (xbee.readPacket(XBEE_STATUS_TIMEOUT)) {
		//if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
			//xbee.getResponse().getZBTxStatusResponse(txStatus);
			//if (txStatus.getDeliveryStatus() == SUCCESS) {
				//return true;
			//} else {
				//writeLcd("Bad Tx Status");
			//}
		//} else {
			//writeLcd("Unexpected Packet|Expected Tx Status");
		//}
	//} else {
	    //writeLcd("Tx Status Timeout|Is the Server Down?");
	//}		
	//return false;
//}

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

uint16_t getBatteryLevel(void) {
	digitalWrite(BAT_SENSE_EN_PIN, HIGH);
	delay(1);
	uint16_t batLevel = analogRead(0);
	digitalWrite(BAT_SENSE_EN_PIN, LOW);
	return batLevel;
}

void beep(boolean longBeep) {
	for(uint16_t i=0; i<((longBeep)? 500 : 50); i++) {
		digitalWrite(SPKR_PIN, HIGH);
		delayMicroseconds(400);
		digitalWrite(SPKR_PIN, LOW);
		delayMicroseconds(400);
	}
}
	
uint16_t getBssEnd(void) {
	extern uint16_t __bss_end;
	return (uint16_t)&__bss_end;
}

uint16_t getStackEnd(void) {
	return ((SPH << 8) | SPL);
}	