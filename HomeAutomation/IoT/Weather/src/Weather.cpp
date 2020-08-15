/******************************************************/
//       THIS IS A GENERATED FILE - DO NOT EDIT       //
/******************************************************/

#line 1 "/Users/johnzet/projects/Home/HomeAutomation/IoT/Weather/src/Weather.ino"
// /*
//  * Project Weather
//  * Description:
//  * Author:
//  * Date:
//  */

// // setup() runs once, when the device is first turned on.
// void setup() {
//   // Put initialization like pinMode and begin functions here.

// }

// // loop() runs over and over again, as quickly as it can execute.
// void loop() {
//   // The core of your code will likely live here.

// }


#include "Particle.h"
#include "MLX90393.h"
#include <math.h>

// This example does not require the cloud so you can run it in manual mode or
// normal cloud-connected mode
void setup();
void loop();
void setupLCD();
void debugPrint(const char* message);
#line 27 "/Users/johnzet/projects/Home/HomeAutomation/IoT/Weather/src/Weather.ino"
SYSTEM_MODE(MANUAL);

const size_t UART_TX_BUF_SIZE = 20;

float readCompass();
void rewriteLcd(const char* msg);
void onDataReceived(const uint8_t* data, size_t len, const BlePeerDevice& peer, void* context);

// These UUIDs were defined by Nordic Semiconductor and are now the defacto standard for
// UART-like services over BLE. Many apps support the UUIDs now, like the Adafruit Bluefruit app.
const BleUuid serviceUuid("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
const BleUuid rxUuid("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
const BleUuid txUuid("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

BleCharacteristic txCharacteristic("tx", BleCharacteristicProperty::NOTIFY, txUuid, serviceUuid);
BleCharacteristic rxCharacteristic("rx", BleCharacteristicProperty::WRITE_WO_RSP, rxUuid, serviceUuid, onDataReceived, NULL);

MLX90393 mlx;
char buffer[100];

void onDataReceived(const uint8_t* data, size_t len, const BlePeerDevice& peer, void* context) {
    // Log.trace("Received data from: %02X:%02X:%02X:%02X:%02X:%02X:", peer.address()[0], peer.address()[1], peer.address()[2], peer.address()[3], peer.address()[4], peer.address()[5]);

    for (size_t ii = 0; ii < len; ii++) {
        Serial.write(data[ii]);
    }
}

void setup() {
    Serial.begin();
    setupLCD();

    BLE.on();
    BLE.addCharacteristic(txCharacteristic);
    BLE.addCharacteristic(rxCharacteristic);
    BleAdvertisingData data;
    data.appendServiceUUID(serviceUuid);
    BLE.advertise(&data);

    Wire.begin();
    // Wire.setSpeed(CLOCK_SPEED_100KHZ);
    // Magnetometer
    // I2C
    // Arduino A4 = SDA
    // Arduino A5 = SCL
    // DRDY ("Data Ready"line connected to A3 (omit third parameter to used timed reads)
    // uint8_t status = mlx.begin(0, 0, A3);
    /* uint8_t status = */ mlx.begin(0, 0, -1, Wire);

    mlx.writeRegister(MLX90393::GAIN_SEL_REG, 0);
    mlx.writeRegister(MLX90393::HALLCONF_REG, 0);
    mlx.writeRegister(MLX90393::TCMP_EN_REG, 0);
    mlx.writeRegister(MLX90393::BURST_SEL_REG, 0);
    mlx.writeRegister(MLX90393::RES_XYZ_REG, 0);
    mlx.writeRegister(MLX90393::RES_XYZ_REG, 0);
    mlx.writeRegister(MLX90393::DIG_FLT_REG, 7);
    mlx.writeRegister(MLX90393::OSR_REG, 0);
    mlx.writeRegister(MLX90393::X_OFFSET_REG, 0);
    mlx.writeRegister(MLX90393::Y_OFFSET_REG, 0);
}

void loop() {
    float heading = readCompass();  // contains a delay
    sprintf(buffer, "Heading %03.1f", heading);
    rewriteLcd(buffer);

    if (BLE.connected()) {        
        txCharacteristic.setValue(buffer);
    }
}

void setupLCD() {
     Serial1.begin(9600); //Begin communication with OpenLCD

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write('-'); //Clear
    
    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write(128 + 0); //Set white/red backlight amount to 0-29    

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write(158 + 15); //Set green backlight amount to 0-29

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write(188 + 0); //Set blue backlight amount to 0-29

    Serial1.write("Hello");
}

void rewriteLcd(const char* msg) {
    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write('-'); //Clear
    Serial1.write(buffer);
}

void debugPrint(const char* message) {
    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write('-'); //Clear
    Serial1.write(message);
}

float readCompass() {
    mlx.sendCommand(MLX90393::CMD_START_MEASUREMENT | MLX90393::X_FLAG | MLX90393::Y_FLAG);
    delay(500);
    MLX90393::txyzRaw data;
    const uint8_t status = mlx.readMeasurement(MLX90393::X_FLAG | MLX90393::Y_FLAG, data);
    if (status == MLX90393::STATUS_ERROR) {
        sprintf(buffer, "Status = %0X", status);
        debugPrint(buffer);
        delay(1000);
    }

    float heading = atan2f(float(int16_t(data.y)), float(int16_t(-data.x)));
    if (heading < 0) {
        heading += 2*3.14159;
    }
    return heading * (360.0/(2*3.14159));
}





