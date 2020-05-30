#include "Serial_LCD_SparkFun.h"
#include "HttpClient.h"
#include "SparkFunBME280.h"
#include "Wire.h"

Serial_LCD_SparkFun lcd;
BME280 bme280;
HttpClient httpClient;
http_request_t httpRequest;
http_response_t httpResponse;
char buffer[1024];
char sensorData[10];
int sensorByteIndex;
int sensorByte;
int loopCounter = 0;

SYSTEM_MODE(SEMI_AUTOMATIC)
SYSTEM_THREAD(ENABLED)
void setup()
{
  Serial.begin(9600);
  Serial1.begin(9600);
    
  lcd.setBrightness(5);
  
  sensorByte = 0;

  Wire.begin();
  bme280.settings.commInterface = I2C_MODE;
  bme280.settings.I2CAddress = 0x77;

    //tStandby can be:
    //  0, 0.5ms
    //  1, 62.5ms
    //  2, 125ms
    //  3, 250ms
    //  4, 500ms
    //  5, 1000ms
    //  6, 10ms
    //  7, 20ms
    bme280.settings.tStandby = 0;

    //filter can be off or number of FIR coefficients to use:
    //  0, filter off
    //  1, coefficients = 2
    //  2, coefficients = 4
    //  3, coefficients = 8
    //  4, coefficients = 16
    bme280.settings.filter = 0;

    //tempOverSample can be:
    //  0, skipped
    //  1 through 5, oversampling *1, *2, *4, *8, *16 respectively
    bme280.settings.tempOverSample = 1;

    //pressOverSample can be:
    //  0, skipped
    //  1 through 5, oversampling *1, *2, *4, *8, *16 respectively
    bme280.settings.pressOverSample = 1;

    //humidOverSample can be:
    //  0, skipped
    //  1 through 5, oversampling *1, *2, *4, *8, *16 respectively
    bme280.settings.humidOverSample = 1;
    delay(10);  //Make sure sensor had enough time to turn on. BME280 requires 2ms to start up.         Serial.begin(57600);

    bme280.begin();

  httpRequest.ip = IPAddress(192, 168, 1, 3);
  httpRequest.port = 8004;
  httpRequest.path = String("/api/v1/dNEFRG25CFJgMXKkvV82/telemetry");
}

void loop()
{


    int pm25, pm10;
    
    int sensorByte = Serial1.read();
    if (sensorByte < 0 ) {
        return;
    }
    sensorData[sensorByteIndex++] = sensorByte & 0xFF;

    if (sensorByteIndex >= 10) {
        sensorByteIndex = 0;

        if (sensorData[0] == 0xAA && sensorData[9] == 0xAB) {
            pm25 = sensorData[2] + sensorData[3] * 256;
            pm10 = sensorData[4] + sensorData[5] * 256;
    
            
    
        }
        loopCounter++;
    }
    if (loopCounter >= 5) {
        loopCounter = 0;

    //runMode can be:
    //  0, Sleep mode
    //  1 or 2, Forced mode
    //  3, Normal mode
        bme280.settings.runMode = 3;
delay(1000);

            lcd.clear();
            lcd.home();

            lcd.selectLine(1);
            Serial1.print("P2.5=");
            Serial1.print(pm25, DEC);
            Serial1.print(" P10=");
            Serial1.print(pm10, DEC);

            lcd.selectLine(2);
            //Serial1.print(bme280.readTempC(), 0);
            //Serial1.print("C ");
            Serial1.print(bme280.readTempF(), 0);
            Serial1.print("F ");
            Serial1.print(bme280.readFloatHumidity(), 0);
            Serial1.print("% ");
            Serial1.print(bme280.readFloatPressure()/82.5, 0);
            Serial1.print("hPa");
            

    bme280.settings.runMode = 0;
    }
    
}



