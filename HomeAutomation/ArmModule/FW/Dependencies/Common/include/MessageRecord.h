#ifndef MESSAGE_RECORD_H
#define MESSAGE_RECORD_H

#include <stm32f4xx_conf.h>
#include <stm32f4xx_rtc.h>
#include <string.h>
#include <Clock.h>

extern Clock* clock;


class MessageRecord {

    public:
        MessageRecord(char* text);
        ~MessageRecord();
        RTC_DateTypeDef date;
        RTC_TimeTypeDef time;
        char* text;
        MessageRecord* next;
};

#endif