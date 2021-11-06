#include "application.h"
#include "MQTT.h"
#include "Serial_LCD_SparkFun.h"
#include "SparkFunBME280.h"
#include "Wire.h"

Serial_LCD_SparkFun lcd;
BME280 bme280;
char buffer[1024];
char sensorData[10];
int sensorByteIndex;
int sensorByte;

void callback(char* topic, byte* payload, unsigned int length);
MQTT mqttClient("192.168.100.2", 1883, 90 /* keepalive timeout */, callback, MQTT_MAX_PACKET_SIZE);

String topicTemperatureConfig = String("homeassistant/sensor/outdoor1/temperature/config");
String topicHumidityConfig =    String("homeassistant/sensor/outdoor1/humidity/config");
String topicPressureConfig =    String("homeassistant/sensor/outdoor1/pressure/config");
String topicState =             String("homeassistant/sensor/outdoor1/state");
boolean mqttConfigured = false;

// recieve message
void callback(char* topic, byte* payload, unsigned int length) {
    char p[length + 1];
    memcpy(p, payload, length);
    p[length] = NULL;
}

// SYSTEM_MODE(SEMI_AUTOMATIC)
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

    //runMode can be:
    //  0, Sleep mode
    //  1 or 2, Forced mode
    //  3, Normal mode
    bme280.settings.runMode = 3;

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
}

void loop()
{

    float tempC = bme280.readTempC();
    float humidity = bme280.readFloatHumidity();
    float pressure = (bme280.readFloatPressure() / 100.0) + 170.2;  // https://novalynx.com/manuals/bp-elevation-correction-tables.pdf

    lcd.clear();
    lcd.home();

    lcd.selectLine(1);
    Serial1.printf("%4.1fC %2.0f%%", tempC, humidity);            
  
    lcd.selectLine(2);
    Serial1.printf("%6.1f mb", pressure);            

    
    if (!mqttClient.isConnected()) {
        mqttConfigured = false;
        mqttClient.connect("outdoor1", "outdoor1", "outdoor1");
    }

    if (mqttClient.isConnected()) {
        // https://www.home-assistant.io/docs/mqtt/discovery/
        if (!mqttConfigured) {
            mqttClient.publish(topicTemperatureConfig, "{\"device_class\": \"temperature\", \"name\": \"Temperature\", \"state_topic\": \"" + topicState + "\", \"unit_of_measurement\": \"°C\", \"value_template\": \"{{ value_json.temperature}}\" }");
            mqttClient.publish(topicHumidityConfig, "{\"device_class\": \"humidity\", \"name\": \"Humidity\", \"state_topic\": \"" + topicState + "\", \"unit_of_measurement\": \"%\", \"value_template\": \"{{ value_json.humidity}}\" }");
            mqttConfigured = true;
        }
        // sprintf(buffer, "{\"temperature\": %4.1f, \"humidity\": %2.0f, \"pressure\": %6.1f}", tempC, humidity, pressure);
        sprintf(buffer, "{\"temperature\": %4.1f, \"humidity\": %2.0f}", tempC, humidity);
        mqttClient.publish(topicState, buffer);
    }

    for (int i=0; i<1000; i++) {
        mqttClient.loop();
        delay(10);
    }
    
}




