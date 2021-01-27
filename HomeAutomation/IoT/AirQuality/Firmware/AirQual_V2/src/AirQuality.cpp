/******************************************************/
//       THIS IS A GENERATED FILE - DO NOT EDIT       //
/******************************************************/

#include "Particle.h"
#line 1 "/Users/johnzet/projects/Home/HomeAutomation/IoT/AirQuality/Firmware/AirQual_V2/src/AirQuality.ino"
void setup();
void loop();
#line 1 "/Users/johnzet/projects/Home/HomeAutomation/IoT/AirQuality/Firmware/AirQual_V2/src/AirQuality.ino"
#pragma GCC push_options
#pragma GCC optimize ("O0")

#include "sensirion_uart.h"
#include "sps30.h"
// #include "Serial_LCD_SparkFun.h"
// #include "HttpClient.h"

// #include "SparkFunBME280.h"
// #include "Wire.h"

// Serial_LCD_SparkFun lcd;
// BME280 bme280;
// HttpClient httpClient;
// http_request_t httpRequest;
// http_response_t httpResponse;
// char buffer[1024];
// char sensorData[10];
// int sensorByteIndex;
// int sensorByte;
// int loopCounter = 0;

// SYSTEM_MODE(SEMI_AUTOMATIC)
// SYSTEM_THREAD(ENABLED)

int sps30_setup(void);
int sps30_measure(void);

void setup()
{
// #if defined(DEBUG_BUILD)
  // Mesh.off();
  BLE.off();
// #endif

// Sensirion driver https://github.com/Sensirion/embedded-uart-sps

    Serial.begin(115200);
    Serial1.begin(115200);

    Serial.println("in setup");

    sps30_setup();

//   lcd.setBrightness(5);
  
//   sensorByte = 0;

//   Wire.begin();
//   bme280.settings.commInterface = I2C_MODE;
//   bme280.settings.I2CAddress = 0x77;

//     //tStandby can be:
//     //  0, 0.5ms
//     //  1, 62.5ms
//     //  2, 125ms
//     //  3, 250ms
//     //  4, 500ms
//     //  5, 1000ms
//     //  6, 10ms
//     //  7, 20ms
//     bme280.settings.tStandby = 0;

//     //filter can be off or number of FIR coefficients to use:
//     //  0, filter off
//     //  1, coefficients = 2
//     //  2, coefficients = 4
//     //  3, coefficients = 8
//     //  4, coefficients = 16
//     bme280.settings.filter = 0;

//     //tempOverSample can be:
//     //  0, skipped
//     //  1 through 5, oversampling *1, *2, *4, *8, *16 respectively
//     bme280.settings.tempOverSample = 1;

//     //pressOverSample can be:
//     //  0, skipped
//     //  1 through 5, oversampling *1, *2, *4, *8, *16 respectively
//     bme280.settings.pressOverSample = 1;

//     //humidOverSample can be:
//     //  0, skipped
//     //  1 through 5, oversampling *1, *2, *4, *8, *16 respectively
//     bme280.settings.humidOverSample = 1;
//     delay(10);  //Make sure sensor had enough time to turn on. BME280 requires 2ms to start up.         Serial.begin(57600);

//     bme280.begin();

//   httpRequest.ip = IPAddress(192, 168, 1, 3);
//   httpRequest.port = 8004;
//   httpRequest.path = String("/api/v1/dNEFRG25CFJgMXKkvV82/telemetry");
}

void loop()
{
    delay(2000);

    sps30_measure();

//    int pm25=0, pm10=0;
//    
//    int sensorByte = Serial.read();
//     if (sensorByte < 0 ) {
//         return;
//     }
//     sensorData[sensorByteIndex++] = sensorByte & 0xFF;

//     if (sensorByteIndex >= 10) {
//         sensorByteIndex = 0;

//         if (sensorData[0] == 0xAA && sensorData[9] == 0xAB) {
//             pm25 = sensorData[2] + sensorData[3] * 256;
//             pm10 = sensorData[4] + sensorData[5] * 256;
    
            
    
//         }
//         loopCounter++;
//     }
//     if (loopCounter >= 5) {
//         loopCounter = 0;

//     //runMode can be:
//     //  0, Sleep mode
//     //  1 or 2, Forced mode
//     //  3, Normal mode
//         bme280.settings.runMode = 3;
// delay(1000);

//             lcd.clear();
//             lcd.home();

//             lcd.selectLine(1);
//             // Serial1.print("P2.5,10um=");
//             Serial1.print("AirQ ");
//             Serial1.print(pm25, DEC);
//             Serial1.print(",");
//             Serial1.print(pm10, DEC);

//             lcd.selectLine(2);
//             //Serial1.print(bme280.readTempC(), 0);
//             //Serial1.print("C ");
//             Serial1.print(bme280.readTempC(), 0);
//             Serial1.print("C ");
//             Serial1.print(bme280.readFloatHumidity(), 0);
//             Serial1.print("% ");
//             Serial1.print(bme280.readFloatPressure()/82.5, 0);
//             Serial1.print("hPa");
            

//     bme280.settings.runMode = 0;
//     }
    
}

int sps30_setup(void) {
    struct sps30_measurement m;
    char serial[SPS30_MAX_SERIAL_LEN];
    const uint8_t AUTO_CLEAN_DAYS = 4;
    int16_t ret;

    while (sensirion_uart_open() != 0) {
        Serial.printf("UART init failed\n");
        delay(1000);
    }

    if (sps30_probe() != 0) {
        Serial.printf("SPS30 sensor probing failed\n");
        return 1;
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
}

int sps30_measure(void) {
    struct sps30_measurement m;
    int16_t ret;

    ret = sps30_start_measurement();
    if (ret < 0) {
        Serial.printf("error starting measurement\n");
    }

    ret = sps30_read_measurement(&m);
    if (ret < 0) {
        Serial.printf("error reading measurement\n");
    } else {
        if (SPS30_IS_ERR_STATE(ret)) {
            Serial.printf(
                "Chip state: %u - measurements may not be accurate\n",
                SPS30_GET_ERR_STATE(ret));
        }

        Serial.printf("measured values:\n"
                "\t%0.2f pm1.0\n"
                "\t%0.2f pm2.5\n"
                "\t%0.2f pm4.0\n"
                "\t%0.2f pm10.0\n"
                "\t%0.2f nc0.5\n"
                "\t%0.2f nc1.0\n"
                "\t%0.2f nc2.5\n"
                "\t%0.2f nc4.5\n"
                "\t%0.2f nc10.0\n"
                "\t%0.2f typical particle size\n\n",
                m.mc_1p0, m.mc_2p5, m.mc_4p0, m.mc_10p0, m.nc_0p5,
                m.nc_1p0, m.nc_2p5, m.nc_4p0, m.nc_10p0,
                m.typical_particle_size);
    }

    /* Stop measurement for 1min to preserve power. Also enter sleep mode
        * if the firmware version is >=2.0.
        */
    ret = sps30_stop_measurement();
    if (ret) {
        Serial.printf("Stopping measurement failed\n");
    }

    // if (version_information.firmware_major >= 2) {
        ret = sps30_sleep();
        if (ret) {
            Serial.printf("Entering sleep failed\n");
        }
    // }

    // if (version_information.firmware_major >= 2) {
        ret = sps30_wake_up();
        if (ret) {
            Serial.printf("Error %i waking up sensor\n", ret);
        }
    // }


   

    return 0;
}

#pragma GCC pop_options

