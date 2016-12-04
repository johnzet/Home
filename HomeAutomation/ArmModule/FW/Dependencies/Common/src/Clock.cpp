#include <Clock.h>


Clock::Clock() {
    bootDate.RTC_Year = 0;
}

void Clock::init()  {

    /* Enable the PWR and BKP Clocks */
    RCC_APB1PeriphClockCmd(RCC_APB1Periph_PWR, ENABLE);

    PWR_BackupAccessCmd(ENABLE);

    RTC_WriteProtectionCmd(DISABLE);
    RCC_LSEConfig(RCC_LSE_ON);
    while(RCC_GetFlagStatus(RCC_FLAG_LSERDY) == RESET);
    RCC_RTCCLKConfig(RCC_RTCCLKSource_LSE);

    RCC_RTCCLKCmd(ENABLE);

//    if(RTC_ReadBackupRegister(RTC_BKP_DR0) != 0x9527)   //A variable, have a look RTC initialization failed
//    {

    RTC_WaitForSynchro();
 
        RTC_InitTypeDef RTC_InitStructure;

        RTC_EnterInitMode();
        RTC_InitStructure.RTC_HourFormat = RTC_HourFormat_24;
        RTC_InitStructure.RTC_AsynchPrediv = 0x7F;
        RTC_InitStructure.RTC_SynchPrediv = 0xFF;
        RTC_Init(&RTC_InitStructure);

        RTC_ExitInitMode();
//        RTC_WriteBackupRegister(RTC_BKP_DR0,0X9527);
        //RTC_WriteProtectionCmd(ENABLE);
//        RTC_WriteBackupRegister(RTC_BKP_DR0,0x9527);  //Initialization is complete, set the flag

//        RTC_WriteBackupRegister(RTC_BKP_DR0,0X9527);
       // RTC_WriteProtectionCmd(ENABLE);
//        RTC_WriteBackupRegister(RTC_BKP_DR0,0x9527);  //Initialization is complete, set the flag
//    }
    //PWR_BackupAccessCmd(DISABLE);
}

void Clock::setClock(uint8_t seconds, uint8_t minutes, uint8_t hours, uint8_t day, uint8_t month, uint8_t weekdayMondayIs1, uint8_t yearYY) {
    RTC_TimeTypeDef RTC_TimeStructure;
    RTC_DateTypeDef RTC_DateStructure;

    RTC_WriteProtectionCmd(DISABLE);
    RTC_EnterInitMode();

    RTC_TimeStructure.RTC_Seconds = seconds;
    RTC_TimeStructure.RTC_Minutes = minutes;
    RTC_TimeStructure.RTC_Hours = hours;
    RTC_TimeStructure.RTC_H12 = RTC_H12_AM;
    RTC_SetTime(RTC_Format_BIN, &RTC_TimeStructure);

    RTC_DateStructure.RTC_Date = day;
    RTC_DateStructure.RTC_Month = month;
    RTC_DateStructure.RTC_WeekDay= weekdayMondayIs1;
    RTC_DateStructure.RTC_Year = yearYY;
    RTC_SetDate(RTC_Format_BIN, &RTC_DateStructure);

    RTC_ExitInitMode();
    RTC_WriteProtectionCmd(ENABLE);

    if (bootDate.RTC_Year < 16) {
        bootDate = RTC_DateStructure;
        bootTime = RTC_TimeStructure;
    }
}

void Clock::prettyPrint(char* buffer) {
    RTC_DateTypeDef date = getDate();
    RTC_TimeTypeDef time = getTime();
    prettyPrint(buffer, &date, &time);
}

void Clock::prettyPrint(char* buffer, RTC_DateTypeDef* date, RTC_TimeTypeDef* time) {
    char* months[12] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    char* days[7] = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    uint8_t weekDay = date->RTC_WeekDay;
    uint8_t day = date->RTC_Date;
    uint8_t month = date->RTC_Month;
    uint16_t year = date->RTC_Year;
    uint8_t hour = time->RTC_Hours;
    uint8_t minute = time->RTC_Minutes;
    uint8_t second = time->RTC_Seconds;
    char* monthName = months[month-1];
    char* dayName = days[weekDay-1];

    // format: Monday June 7, 2016 10:20:30
    sprintf(buffer, "%s %s %u, 20%02u - %2u:%02u:%02u", dayName, monthName, day, year, hour, minute, second);
}

RTC_DateTypeDef Clock::getDate() {
    RTC_DateTypeDef date;
    RTC_GetDate(RTC_Format_BIN, &date);
    return date;
}

RTC_TimeTypeDef Clock::getTime() {
    RTC_TimeTypeDef time;
    RTC_GetTime(RTC_Format_BIN, &time);
    return time;
}

uint32_t Clock::getSecondsSince2000(RTC_DateTypeDef* date, RTC_TimeTypeDef* time) {
    uint32_t seconds = 0;

    // Note:  This does not take 30-day months, leap year or anything into account.

    seconds =  time->RTC_Seconds;
    seconds += time->RTC_Minutes * 60;
    seconds += time->RTC_Hours * 3600;

    seconds += date->RTC_Date * 3600 * 24;
    seconds += date->RTC_Month * 3600 * 24 * 31;
    seconds += date->RTC_Year * 3600 * 24 * 365;
    
    return seconds;
}

uint32_t Clock::getSecondsAgo(RTC_DateTypeDef* date, RTC_TimeTypeDef* time) {
    uint32_t seconds1 = getSecondsSince2000(date, time);
    
    RTC_DateTypeDef d = this->getDate();
    RTC_TimeTypeDef t = this->getTime();
    uint32_t seconds2 = getSecondsSince2000(&d, &t);

    return abs(seconds1-seconds2);
}

uint32_t Clock::getUptimeSeconds() {
    if (bootDate.RTC_Year < 16) return 0;
    return getSecondsAgo(&bootDate, &bootTime);
}

void Clock::getUptimeString(char* buffer) {
    uint32_t uptime = getUptimeSeconds();
    if (uptime < 1) {
        sprintf(buffer, "Recently Started");
    } else if (uptime < 60) {
        sprintf(buffer, "%i   Seconds Uptime", uptime);
    } else if (uptime < 3600) {
        sprintf(buffer, "%u   Minutes Uptime", uptime/60);
    } else if (uptime < 24*3600) {
        sprintf(buffer, "%u   Hours Uptime", uptime/3600);
    } else {
        sprintf(buffer, "%u   Days Uptime", uptime/(unsigned int)(24*3600));
    }
}





