#ifndef CLOCK_H
#define CLOCK_H

#pragma GCC diagnostic ignored "-Wwrite-strings"

#include <stm32f4xx_conf.h>
#include <stm32f4xx_rtc.h>
#include <stm32f4xx_rcc.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#define HEX_2_DEC(val) (((val)/16)*10+((val)%16))
#define DEC_2_HEX(val) (((val)/10)*16+((val)%10))


class ClockData {
    public:
        uint8_t second;
        uint8_t minute;
        uint8_t hour;
        uint8_t day;
        uint8_t month;
        uint8_t year;
};



class Clock {
    public:
        Clock();
        void init();
        void setClock(uint8_t seconds, uint8_t minutes, uint8_t hours, uint8_t day, uint8_t month, uint8_t weekdayMondayIs1, uint8_t yearYY);
        RTC_DateTypeDef getDate();
        RTC_TimeTypeDef getTime();
        void prettyPrint(char* buffer);
        void prettyPrint(char* buffer, RTC_DateTypeDef* date, RTC_TimeTypeDef* time);
        uint32_t getSecondsSince2000(RTC_DateTypeDef* date, RTC_TimeTypeDef* time);
        uint32_t getSecondsAgo(RTC_DateTypeDef* date, RTC_TimeTypeDef* time);
        uint32_t getUptimeSeconds();
        void getUptimeString(char* buffer);


    private: 
        RTC_DateTypeDef bootDate;
        RTC_TimeTypeDef bootTime;
};

#endif