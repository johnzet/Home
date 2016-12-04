#include <WaterSensorSample.h>



WaterSensorSample::WaterSensorSample() {
    this->name[0] = NULL;
    this->address = 0;
    this->wet = false;
    this->voltage = 0;
    this->rssi = 0;
    RTC_DateStructInit(&this->date);
    RTC_TimeStructInit(&this->time);
}

WaterSensorSample::~WaterSensorSample() {
}

char* WaterSensorSample::getName() { return this->name; }
uint64_t WaterSensorSample::getAddress() { return this->address; }
float WaterSensorSample::getBatteryVoltage() { return this->voltage; }
bool WaterSensorSample::isWet() { return this->wet; }
uint8_t WaterSensorSample::getRssi() { return this->rssi; }
RTC_DateTypeDef WaterSensorSample::getDate() {return this->date;}
RTC_TimeTypeDef WaterSensorSample::getTime() {return this->time;}

void WaterSensorSample::setName(char* buffer, uint32_t length) {
    memcpy(this->name, buffer, length); this->name[length] = NULL; 
}
void WaterSensorSample::setAddress(uint64_t address) { 
    assert(address > 0xFFFFFFFF);
    this->address = address; 
}
void WaterSensorSample::setBatteryVoltage(float voltage) { this->voltage = voltage; }
void WaterSensorSample::setIsWet(bool isWet) { this->wet = isWet; }
void WaterSensorSample::setRssi(uint8_t rssi) { this->rssi = rssi;}
void WaterSensorSample::setDate(RTC_DateTypeDef date) {this->date = date;}
void WaterSensorSample::setTime(RTC_TimeTypeDef time) {this->time = time;}
