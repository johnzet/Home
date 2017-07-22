#ifndef WATER_SENSOR_SAMPLE_H
#define WATER_SENSOR_SAMPLE_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <task.h>
#include <stdio.h>
#include <string.h>
#include <stm32f4xx_rtc.h>
#include <assert.h>


class WaterSensorSample {

    private:
        uint64_t address;
        char name[21];
        float voltage;
        bool wet;
        uint8_t rssi;
        RTC_DateTypeDef date;
        RTC_TimeTypeDef time;


    public:
        WaterSensorSample();
        ~WaterSensorSample();

        void setName(char* buffer, uint32_t length);
        void setAddress(uint64_t);
        void setBatteryVoltage(float voltage);
        void setIsWet(bool isWet);
        void setRssi(uint8_t rssi);
        void setDate(RTC_DateTypeDef date);
        void setTime(RTC_TimeTypeDef time);


        char* getName();
        uint64_t getAddress();
        float getBatteryVoltage();
        bool isWet();
        uint8_t getRssi();
        RTC_DateTypeDef getDate();
        RTC_TimeTypeDef getTime();
};




#endif