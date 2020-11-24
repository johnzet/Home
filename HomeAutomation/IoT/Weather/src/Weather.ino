#include "Particle.h"
#include "MLX90393.h"
#include <math.h>
#include "SparkFunBME280.h"

SYSTEM_MODE(MANUAL);

float readCompass();
void rewriteLcd(const char* msg);
void onDataReceived(const uint8_t* data, size_t len, const BlePeerDevice& peer, void* context);
void setBluetoothDataBytes();
float convertKphToMph(float kph);
float convertKphToMps(float kph);
float convertCToF(float degC);
float convertMbarToInHg(float mBar);

// GATT Characteristics https://www.bluetooth.com/specifications/gatt/characteristics/
// Some guy's Kestrel notes:  https://bad-radio.solutions/notes_ble
// A virus report which sheds light From https://www.hybrid-analysis.com/sample/ed3c0e341b224014088381590c1f817acdabcf26015cff7ae63f1c454a0f3bd5?environmentId=200

const BleUuid kestrelServiceUuid(               "03290000-EAB4-DEA1-B24E-44EC023874DB");
const BleUuid deviceInfoServiceUuid(            "0000180a-0000-1000-8000-00805f9b34fb");
const BleUuid batteryServiceUuid(               "0000180f-0000-1000-8000-00805f9b34fb");

const BleUuid mfgNameUuid(                      "00002a29-0000-1000-8000-00805f9b34fb");
const BleUuid deviceNameUuid(                   "00002a00-0000-1000-8000-00805f9b34fb");
const BleUuid appearanceUuid(                   "00002a01-0000-1000-8000-00805f9b34fb");
const BleUuid serialNumberUuid(                 "00002a25-0000-1000-8000-00805f9b34fb");
const BleUuid hardwareVersionUuid(              "00002a27-0000-1000-8000-00805f9b34fb");
const BleUuid firmwareVersionUuid(              "00002a26-0000-1000-8000-00805f9b34fb");
const BleUuid softwareVersionUuid(              "00002a28-0000-1000-8000-00805f9b34fb");
const BleUuid modelNumberUuid(                  "00002a24-0000-1000-8000-00805f9b34fb");

const BleUuid characteristic16bitUuid(          "03290310-EAB4-DEA1-B24E-44EC023874DB");
const BleUuid characteristic32bitUuid(          "03290320-EAB4-DEA1-B24E-44EC023874DB");

const BleUuid batteryLevelUuid(                 "00002a19-0000-1000-8000-00805f9b34fb");

BleCharacteristic deviceNameCharacteristic("device name", BleCharacteristicProperty::READ, deviceNameUuid, kestrelServiceUuid);
BleCharacteristic appearanceCharacteristic("Appearance", BleCharacteristicProperty::READ, appearanceUuid, kestrelServiceUuid);
BleCharacteristic mfgNameCharacteristic("mfg name", BleCharacteristicProperty::READ, mfgNameUuid, deviceInfoServiceUuid);
BleCharacteristic modelNumberCharacteristic("model number", BleCharacteristicProperty::READ, modelNumberUuid, deviceInfoServiceUuid);
BleCharacteristic serialNumberCharacteristic("serial number", BleCharacteristicProperty::READ, serialNumberUuid, deviceInfoServiceUuid);
BleCharacteristic hardwareVersionCharacteristic("hardware version", BleCharacteristicProperty::READ, hardwareVersionUuid, deviceInfoServiceUuid);
BleCharacteristic firmwareVersionCharacteristic("firmware version", BleCharacteristicProperty::READ, firmwareVersionUuid, deviceInfoServiceUuid);
BleCharacteristic softwareVersionCharacteristic("software version", BleCharacteristicProperty::READ, softwareVersionUuid, deviceInfoServiceUuid);

BleCharacteristic characteristic16bit("Characteristic 16-bit", BleCharacteristicProperty::READ | BleCharacteristicProperty::NOTIFY, characteristic16bitUuid, kestrelServiceUuid);
BleCharacteristic characteristic32bit("Characteristic 32-bit", BleCharacteristicProperty::READ | BleCharacteristicProperty::NOTIFY, characteristic32bitUuid, kestrelServiceUuid);

BleCharacteristic batteryLevelCharacteristic("Battery Lavel", BleCharacteristicProperty::READ | BleCharacteristicProperty::NOTIFY, batteryLevelUuid, batteryServiceUuid);


MLX90393 mlx;
BME280 bme280;
char buffer1[100];
// char buffer2[100];
system_tick_t previousMicroseconds = 0;
system_tick_t currentMicroseconds = 0;
int screenNumber = 0;

struct sampleBytes_t {
    uint16_t windSpeed;
    uint16_t windDirection;
    uint16_t temperature;
    uint16_t humidity;
    uint16_t pressure;
    uint32_t altitude;
};

struct sample_t {
    float windSpeedMps;
    float windDirectionDeg;
    float temperatureC;
    float humidity;
    float pressureBar;
    float altitudeM;
};

void setBluetoothDataBytes(sampleBytes_t sampleBytes);
void setBluetoothData(sample_t sample);

void onDataReceived(const uint8_t* data, size_t len, const BlePeerDevice& peer, void* context) {
    for (size_t ii = 0; ii < len; ii++) {
        Serial.write(data[ii]);
    }
}

// --------------------------------------------------------------------------- setup ------------------------------------------------------------------
void setup() {
    Serial.begin();
    pinMode(A2, INPUT);
    setupLCD();
    setupBLE();
    setupMagnetometer();
    setupHallSensor();
    setupBME280();
}

// ---------------------------------------------------------------------------- loop -------------------------------------------------------------------
void loop() {
    float windSpeedKph = 0.0;
    if ((micros() - currentMicroseconds) < (1000.0 * 1000.0 * 20.0)) {
        float periodS = float(currentMicroseconds - previousMicroseconds) / (1000.0 * 1000.0);
        if (periodS < 20.0 && periodS > 0.1) {
            windSpeedKph = 29.0 / periodS;
        }
    } else {
        previousMicroseconds = micros();
        currentMicroseconds = previousMicroseconds;
    }
    float heading = readCompass();  // contains a delay
    float battVoltage = analogRead(A2) * 1.168 / 1024;

    delay(500);
    if (BLE.connected()) {   
        batteryLevelCharacteristic.setValue((uint8_t)(90 + (rand() % 10)));

        sample_t sample;
        sample.windSpeedMps =       convertKphToMps(windSpeedKph);
        sample.temperatureC =       bme280.readTempC();
        sample.humidity =           bme280.readFloatHumidity();
        sample.pressureBar =        bme280.readFloatPressure()/100000.0f;
        sample.windDirectionDeg =   heading;
        sample.altitudeM =          1.483;  // Briggsdale, CO = 4865 ft

        setBluetoothData(sample);
    }

    uint8_t degreeSymbol = 0b11011111;

    switch (screenNumber) {
        case 0:
        case 1:
            sprintf(buffer1, "%4.1f MPH  %3.0f%c  %3.0f%cF  %2.0f%%", convertKphToMph(windSpeedKph), heading, degreeSymbol, bme280.readTempF(), degreeSymbol, bme280.readFloatHumidity());
            screenNumber++;
            break;
        case 2:
        case 3:
            sprintf(buffer1, "%4.1f MPH  %3.0f%c  %5.1f mBar %3.1fV", convertKphToMph(windSpeedKph), heading, degreeSymbol, bme280.readFloatPressure()/100.0f, battVoltage);
            screenNumber++;
            if (screenNumber == 4) {
                screenNumber = 0;
            }
            break;
        default:
            sprintf(buffer1, "Wrong screen number");
            screenNumber = 0;
    }
    rewriteLcd(buffer1);
}

void setBluetoothData(sample_t sample) {
    sampleBytes_t sampleBytes;

    sampleBytes.windSpeed = static_cast<uint16_t>(sample.windSpeedMps * 1000);
    sampleBytes.temperature = static_cast<uint16_t>(sample.temperatureC * 100);
    sampleBytes.humidity = static_cast<uint16_t>(sample.humidity * 100);
    sampleBytes.pressure = static_cast<uint16_t>(sample.pressureBar * 10000);
    sampleBytes.windDirection = static_cast<uint16_t>(sample.windDirectionDeg);
    sampleBytes.altitude = static_cast<uint32_t>(sample.altitudeM * 10000);

    setBluetoothDataBytes(sampleBytes);
}

void setBluetoothDataBytes(sampleBytes_t sampleBytes) {
    
    // // 16-bit int LSB      wind speed m/s     temp deg C                            humidity 72.8%     pressure mbar*10   wind dir deg                             
    // uint8_t data16bit[] = {0x76, 0x11,        0x2B, 0x02,        0x00, 0x00,        0x70, 0x1C,        0xC0, 0x21,        0x7B, 0x00};
    // // 32-bit int LSB                                     altitude m*10
    // uint8_t data32bit[] = {0x00, 0x00, 0x00, 0x00,        0xEE, 0x39, 0x00, 0x00,        0x00, 0x00, 0x00, 0x00};

    size_t bufferSize = 12;
    uint8_t data16bit[bufferSize]; 
    uint8_t data32bit[bufferSize]; 
    memset(data16bit, 0, bufferSize);
    memset(data32bit, 0, bufferSize);

    // memcpy will copy in reverse order since this is a little-endian machine
    memcpy(data16bit+0, &sampleBytes.windSpeed, sizeof(sampleBytes.windSpeed));
    memcpy(data16bit+2, &sampleBytes.temperature, sizeof(sampleBytes.temperature));
    memcpy(data16bit+6, &sampleBytes.humidity, sizeof(sampleBytes.humidity));
    memcpy(data16bit+8, &sampleBytes.pressure, sizeof(sampleBytes.pressure));
    memcpy(data16bit+10, &sampleBytes.windDirection, sizeof(sampleBytes.windDirection));
    memcpy(data32bit+4, &sampleBytes.altitude, sizeof(sampleBytes.altitude));
    
    characteristic16bit.setValue(data16bit, 12);
    characteristic32bit.setValue(data32bit, 12);
}

void setupLCD() {
     Serial1.begin(9600); //Begin communication with OpenLCD
    
    delay(250);

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write('-'); //Clear

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write(128 + 0); //Set white/red backlight amount to 0-29    

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write(158 + 0); //Set green backlight amount to 0-29

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write(188 + 15); //Set blue backlight amount to 0-29

    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write('-'); //Clear

    Serial1.write("John Zehetner     Fake Kestrel");
    delay(3000);
}

void setupBLE() {
    BLE.on();
    BLE.addCharacteristic(modelNumberCharacteristic);
    BLE.addCharacteristic(mfgNameCharacteristic);
    BLE.addCharacteristic(deviceNameCharacteristic);
    BLE.addCharacteristic(appearanceCharacteristic);
    BLE.addCharacteristic(serialNumberCharacteristic);
    BLE.addCharacteristic(hardwareVersionCharacteristic);
    BLE.addCharacteristic(firmwareVersionCharacteristic);
    BLE.addCharacteristic(softwareVersionCharacteristic);

    BLE.addCharacteristic(characteristic16bit);
    BLE.addCharacteristic(characteristic32bit);
    BLE.addCharacteristic(batteryLevelCharacteristic);
    
    mfgNameCharacteristic.setValue("Kestrel by NK");
    deviceNameCharacteristic.setValue("FIRE - 2334359");
    appearanceCharacteristic.setValue("");
    modelNumberCharacteristic.setValue("5500FWL");
    serialNumberCharacteristic.setValue("2334359");
    hardwareVersionCharacteristic.setValue("Rev 11B");
    firmwareVersionCharacteristic.setValue("1.21");
    softwareVersionCharacteristic.setValue("");

    BleAdvertisingData data;
    data.appendLocalName("FIRE");  // don't change this
    data.appendServiceUUID(kestrelServiceUuid);
    // data.appendServiceUUID(batteryServiceUuid);  // won't pair with this added
    BLE.advertise(&data);
}

void setupMagnetometer() {
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

void setupHallSensor() {
    void hallSensorInterrupt();
    byte hallSensorPin = D2;
    pinMode(hallSensorPin, INPUT);
    attachInterrupt(hallSensorPin, hallSensorInterrupt, FALLING);
}

void setupBME280() {
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

    //runMode can be:
    //  0, Sleep mode
    //  1 or 2, Forced mode
    //  3, Normal mode
    bme280.settings.runMode = 3;
    
    bme280.begin();
}

void rewriteLcd(const char* msg) {
    Serial1.write('|'); //Put LCD into setting mode
    Serial1.write('-'); //Clear
    Serial1.write(msg);
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
        sprintf(buffer1, "Status = %0X", status);
        debugPrint(buffer1);
        delay(1000);
    }

    float heading = atan2f(float(int16_t(-data.y)), float(int16_t(data.x)));
    if (heading < 0) {
        heading += 2*3.14159;
    }
    return heading * (360.0/(2*3.14159));
}

void hallSensorInterrupt() {
    if (previousMicroseconds <=0) {
        previousMicroseconds = micros();
        currentMicroseconds = previousMicroseconds;
        return;
    }
    previousMicroseconds = currentMicroseconds;
    currentMicroseconds = micros();   
}

float convertKphToMph(float kph) {
    return kph * 0.6214;
}

float convertCToF(float degC) {
    return 32.0 + 1.8 * degC;
}

float convertKphToMps(float kph) {
    return kph * 0.2778;
}

float convertMbarToInHg(float mBar) {
    return mBar * 0.02953;
}
