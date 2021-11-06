// #pragma GCC push_options
// #pragma GCC optimize ("O0")

#include "application.h"
#include "MQTT.h"
#include "Wire.h"
#include "SPS30/sensirion_uart.h"  
#include "SPS30/sps30.h"   // Sensirion driver https://github.com/Sensirion/embedded-uart-sps
#include "BME280/BME280.h"
#include "LCD/SerLCD.h"
#include "SGP40/SparkFun_SGP40_Arduino_Library.h"

BME280 bme280;
SGP40 sgp40;
SerLCD lcd;
char buffer[1024];
char mqttBuffer[100];
int loopCounter = 0;
const uint32_t baseColor = 0x808080;
const byte degreeChar = 0x00;
const byte muChar = 0x01;
const byte squaredChar = 0x02;
const byte cubedChar = 0x03;
const byte subTwoChar = 0x04;
const int BUTTON1 = D5;
const int BUTTON2 = D6;
const int maxPageNumber = 5;

void bme280_setup(void);
void sps30_setup(void);
void lcd_setup(void);
void sgp40_setup(void);
sps30_measurement sps30_measure(void);
int pageNumber = 0;

void mqttCallback(char* topic, byte* payload, unsigned int length);
MQTT mqttClient("192.168.100.2", 1883, 90 /* keepalive timeout */, mqttCallback, MQTT_MAX_PACKET_SIZE);
boolean mqttConfigured = false;
std::string outdoor1Topic = std::string("homeassistant/sensor/outdoor1/state");
float outdoorTempC;
float outdoorHumidity = 0;
time32_t mqttLastCallbackTime;

// recieve message
void mqttCallback(char* topic, byte* payload, unsigned int length) {
    strncpy(mqttBuffer, (char *)payload, length);
    mqttBuffer[length] = NULL;
    Serial.printlnf("MQTT Subscription: %s", mqttBuffer);
    JSONValue jsonPayload = JSONValue::parseCopy(mqttBuffer);
    
    JSONObjectIterator iter(jsonPayload);
    while(iter.next()) {
        if (iter.name() == "temperature") {
            outdoorTempC = iter.value().toDouble();
        } else if (iter.name() == "humidity") {
            outdoorHumidity = iter.value().toDouble();
        }
    }
    mqttLastCallbackTime = Time.now();
}

SYSTEM_THREAD(ENABLED)
SYSTEM_MODE(MANUAL)

// --------------------------------------------------- SETUP --------------------------------------------
void setup()
{
    Serial.begin(115200);
    Serial1.begin(115200);

    Serial.println("Starting Air Quality Monitor by John Zehetner");

    setupWiFi();
    
    pinMode(PWR, INPUT);
	pinMode(CHG, INPUT);
    pinMode(BUTTON2, INPUT_PULLUP);
    
    Wire.begin();

    lcd_setup();
    sps30_setup();
    bme280_setup();
    sgp40_setup();

    mqttLastCallbackTime = Time.now();
}

// --------------------------------------------------- LOOP --------------------------------------------
void loop()
{
    if (Particle.connected() == true) {
        Particle.process();
    }

    float tempC = bme280.readTempC();
    float tempF = bme280.readTempF();
    float humidity = bme280.readFloatHumidity();
    int voc = sgp40.getVOCindex(humidity, tempC);
    float voltage = analogRead(BATT) * 0.0011224;
    float batPercent = (voltage-3.3)/0.9 * 100.0; 
	boolean usbPowered = digitalRead(PWR);
	boolean charging = !digitalRead(CHG) && usbPowered;

    struct sps30_measurement airQ = sps30_measure();            
    float standardPressure = 1013.2;
    float pressure = (bme280.readFloatPressure() / 100.0) + 170.2;  // https://novalynx.com/manuals/bp-elevation-correction-tables.pdf

    Serial.printlnf("BME280: %5.2f˚C %2.0f%% %6.1f mBar", tempC, humidity, pressure);
    Serial.printlnf("SPS30:\n"
        "\tPM1.0 %.3g µg/m^3    PM2.5 %.3g µg/m^3\n"
        "\tPM4.0 %.3g µg/m^3    PM10  %.3g µg/m^3\n"
        "\tNC0.5 %.4g #/cm^3    NC1.0 %.4g #/cm^3\n"
        "\tNC2.5 %.4g #/cm^3    NC4.0 %.4g #/cm^3\n"
        "\tNC10  %.4g #/cm^3\n"
        "\tSize %.2g µm",
        airQ.mc_1p0, airQ.mc_2p5, airQ.mc_4p0, airQ.mc_10p0, airQ.nc_0p5,
        airQ.nc_1p0, airQ.nc_2p5, airQ.nc_4p0, airQ.nc_10p0,
        airQ.typical_particle_size);
    Serial.printf("VOC Index = %3i\n", voc);
    Serial.printlnf("Battery voltage %4.2fV %s, %s", voltage, (charging? "Charging" : "Not Charging"), (usbPowered? "USB Connected" : "USB NotConnected"));
    Serial.println("\n");

    sgp40.getVOCindex(humidity, tempC);  // SGP40 datasheet suggests a sample period of 1 second.  loop() is called less often, though.
    lcd.clear();
    sgp40.getVOCindex(humidity, tempC);  // SGP40 datasheet suggests a sample period of 1 second.  loop() is called less often, though.
    switch(pageNumber) {
        case 0:
            if ((Time.now()-mqttLastCallbackTime)>60*2) {
                outdoorHumidity = -1;
                outdoorTempC = 0;
            }
            lcd.printf("In  %.3g", tempC); lcd.writeChar(degreeChar); lcd.printf("C  %2.0f%%", humidity); 
            lcd.setCursor(0, 1);
            if (outdoorHumidity < 0) {
                lcd.println("Out - Check Sensor");
            } else {
                lcd.printf("Out %.3g", outdoorTempC); lcd.writeChar(degreeChar); lcd.printf("C  %2.0f%%", outdoorHumidity);
            }
            lcd.setCursor(0, 2);
            lcd.printf("%6.1fmb  %5.1fmb", pressure, pressure - standardPressure);
            lcd.setCursor(0, 3);
            lcd.printf("PM2.5 %4.0f VOC %3i", airQ.mc_2p5, voc);
            // lcd.printf("%.3g", tempC); lcd.writeChar(degreeChar); lcd.printf("C "); lcd.printf("%.3g", tempF); lcd.writeChar(degreeChar); lcd.printf("F  %2.0f%%", humidity);   
            // lcd.setCursor(0, 1);
            // lcd.printf("%6.1fmb %5.1fmb", pressure, pressure - standardPressure);
            // lcd.setCursor(0, 2);
            // lcd.printf("PM2.5,10 %4.0f %4.0f", airQ.mc_2p5, airQ.mc_10p0);
            // lcd.setCursor(0, 3); 
            // lcd.printf("VOC Index %3i  %3.0f%%", voc, batPercent);  
            break;
        case 1:
            lcd.print("PM1,2.5,4,10 "); lcd.writeChar(muChar); lcd.print("g/m"); lcd.writeChar(cubedChar);
            lcd.setCursor(0, 1);
            lcd.printf("%4.0f  %4.0f", airQ.mc_1p0, airQ.mc_2p5);
            lcd.setCursor(0, 2);
            lcd.printf("%4.0f  %4.0f", airQ.mc_4p0, airQ.mc_10p0);
            lcd.setCursor(0, 3);
            lcd.printf("Typ Size %.2g ", airQ.typical_particle_size); lcd.writeChar(muChar); lcd.print("m");
            break;
        case 2:
            lcd.print("Count 0.5,1,2.5,4,10");
            lcd.setCursor(0, 1);
            lcd.printf("%.4g  #/cm", airQ.nc_0p5); lcd.writeChar(cubedChar);
            lcd.setCursor(0, 2);
            lcd.printf("%.4g  %.4g", airQ.nc_1p0, airQ.nc_2p5);
            lcd.setCursor(0, 3);
            lcd.printf("%.4g  %.4g", airQ.nc_4p0, airQ.nc_10p0);
            break;
        case 3:
            lcd.printf("VOC Index %3i", voc);  
            lcd.setCursor(0, 1);
            lcd.print(" 100 is typical");    
            lcd.setCursor(0, 2);
            lcd.print("   for indoors");    
            lcd.setCursor(0, 3);
            lcd.print(" 500 is max");    
            break;
        case 4:
            lcd.printf("Battery %4.2fV", voltage);
            lcd.setCursor(0, 1);
            lcd.printf("%3.0f%%", batPercent);
            lcd.setCursor(0, 2);
            lcd.print((charging? "Charging" : "Not Charging"));
            lcd.setCursor(0, 3);
            lcd.print((usbPowered? "USB Connected" : "USB Disconnected"));
            break;
        case maxPageNumber:
            lcd.printf("Sensors");
            lcd.setCursor(0, 1);
            lcd.printf(" Bosch BME280");
            lcd.setCursor(0, 2);
            lcd.printf(" Sensirion SPS30");
            lcd.setCursor(0, 3);
            lcd.printf(" Sensirion SGP40");
            break;    
        default:
            pageNumber = 0;
            break;    
    }

    if (!mqttClient.isConnected()) {
        mqttConfigured = false;
        mqttClient.connect("airqual1", "airqual1", "airqual1");
    }

    if (mqttClient.isConnected()) {
        if (!mqttConfigured) {
            mqttClient.subscribe(outdoor1Topic.c_str());
            mqttConfigured = true;
        }
    }

    system_tick_t delayStartTime = millis();
    boolean nextPageTrigger = false;
    while ((millis()-delayStartTime) < 2000 && !nextPageTrigger) {
        if (digitalRead(BUTTON2) == LOW) {
            pageNumber++;
            if (pageNumber > maxPageNumber) {
                pageNumber = 0;
            }
            nextPageTrigger = true;
            lcd.clear();
        } else {
            mqttClient.loop();
            delay(100);
        }
    }
}

void bme280_setup(void) {
    bme280.settings.commInterface = I2C_MODE;
    bme280.settings.I2CAddress = 0x77;

    // BME280 runMode can be:
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
    bme280.settings.filter = 1;

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
    delay(10);  //Make sure sensor had enough time to turn on. BME280 requires 2ms to start up. 

    bme280.begin();
}

void sps30_setup(void) {
    char serial[SPS30_MAX_SERIAL_LEN];
    const uint8_t AUTO_CLEAN_DAYS = 4;
    int16_t ret;

    while (sensirion_uart_open() != 0) {
        Serial.printf("UART init failed\n");
        delay(1000);
    }

    if (sps30_probe() != 0) {
        Serial.printf("SPS30 sensor probing failed\n");
        return;
    }
    Serial.printf("SPS30 sensor probing successful\n");

    struct sps30_version_information version_information;
    ret = sps30_read_version(&version_information);
    if (ret) {
        Serial.printf("error %d reading version information\n", ret);
    } else {
        Serial.printf("FW: %u.%u HW: %u, SHDLC: %u.%u\n",
               version_information.firmware_major,
               version_information.firmware_minor,
               version_information.hardware_revision,
               version_information.shdlc_major,
               version_information.shdlc_minor);
    }

    ret = sps30_get_serial(serial);
    if (ret)
        Serial.printf("error %d reading serial\n", ret);
    else
        Serial.printf("SPS30 Serial: %s\n", serial);

    ret = sps30_set_fan_auto_cleaning_interval_days(AUTO_CLEAN_DAYS);
    if (ret)
        Serial.printf("error %d setting the auto-clean interval\n", ret);

    ret = sps30_start_measurement();
    if (ret < 0) {
        Serial.printf("error starting measurement\n");
    }
}

void setupWiFi() {
    // BLE.selectAntenna(BleAntennaType::EXTERNAL);
    WiFi.on();
    WiFi.selectAntenna(ANT_INTERNAL);
    WiFi.clearCredentials();
    WiFi.setCredentials("zhome", "fydua1vare", WPA2);
    WiFi.connect();
    Particle.connect();
}

void lcd_setup(void) {
    byte degreeCharData[] = {
        0b01100,
        0b10010,
        0b10010,
        0b01100,
        0b00000,
        0b00000,
        0b00000,
        0b00000};

    byte muCharData[] = {
        0b00000,
        0b10001,
        0b10001,
        0b10001,
        0b10011,
        0b11101,
        0b10000,
        0b10000};

    byte squaredCharData[] = {
        0b01100,
        0b10010,
        0b00100,
        0b01000,
        0b11110,
        0b00000,
        0b00000,
        0b00000};

    byte cubedCharData[] = {
        0b11100,
        0b00010,
        0b01100,
        0b00010,
        0b11100,
        0b00000,
        0b00000,
        0b00000};

    byte subTwoCharData[] = {
        0b00000,
        0b00000,
        0b00000,
        0b01100,
        0b10010,
        0b00100,
        0b01000,
        0b11110};

    lcd.begin(Wire);

    lcd.disableSystemMessages();
    if (0) {
        lcd.clear();
        lcd.print("  Starting");
        lcd.saveSplash();
        lcd.enableSplash();
    }

    lcd.createChar(degreeChar, degreeCharData);
    lcd.createChar(muChar, muCharData);
    lcd.createChar(squaredChar, squaredCharData);
    lcd.createChar(cubedChar, cubedCharData);
    lcd.createChar(subTwoChar, subTwoCharData);

    lcd.setFastBacklight(baseColor);
    lcd.clear();
    lcd.println("Air Quality Monitor");
    lcd.setCursor(0, 2);
    lcd.println("    John Zehetner");
}

void sgp40_setup(void) {
    sgp40.enableDebugging(Serial);
    if (sgp40.begin() != SGP40_SUCCESS) {
        Serial.println("SGP40 not detected.");
    }
}

sps30_measurement sps30_measure(void) {
    struct sps30_measurement m;
    int16_t ret;

    ret = sps30_read_measurement(&m);
    if (ret < 0) {
        Serial.printf("error reading measurement\n");
    } else {
        if (SPS30_IS_ERR_STATE(ret)) {
            Serial.printf(
                "Chip state: %u - measurements may not be accurate\n",
                SPS30_GET_ERR_STATE(ret));
        }
    }

    return m;
}

// #pragma GCC pop_options

