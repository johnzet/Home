#ifndef MAIN_TASK_H
#define MAIN_TASK_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <task.h>
#include <timers.h>
#include <queue.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <typeinfo>
#include <TaskWrapper.h>
#include <LEDs.h>
#include <AtCmdPacket.h>
#include <WiFiReceiverTask.h>
#include <WiFiTransmitterTask.h>
#include <XBeeTask.h>
#include <delay.h>
#include <Clock.h>
#include <WaterValve.h>
#include <LCD.h>
#include <Fonts/FontGeorgia16x10.h>
#include <Fonts/FontGeorgia10x7.h>
#include <heap_4.h>
#include <MessageList.h>
#include <SysMonitorTask.h>

#define PIR_PIN         GPIO_Pin_3
#define SWITCH_LED_PIN  GPIO_Pin_10
#define SWITCH_PIN      GPIO_Pin_11

extern LEDs* leds;
extern Clock* clock;
extern LCD* lcd;
extern BlockLink_t *osAllocatedHeapStart;
extern MessageList* messageList;
void blueLedCallback(TimerHandle_t timer);


#pragma GCC diagnostic ignored "-Wunused-parameter"
#define REGISTERED_WATER_SENSOR_COUNT 18



class MainTask : public TaskClass {
  public:
    MainTask(QueueHandle_t sensorSampleQueue, QueueHandle_t httpRequestQueue, WiFiReceiverTask* wiFiReceiverTask, WiFiTransmitterTask* wiFiTransmitterTask, XBeeTask* xbeeTask, WaterValve* waterValve);
    ~MainTask();
    void init();
    bool setup();
    void task();
    void setIsWet(bool isWet);
    bool isWet();
    void createHtpResponse(HttpPacket* packet);
    WaterValve* waterValve;
    void resetState(bool fromIsr);
    uint8_t lcdPageNumber;
    bool getTimeFromServerByHttp();
    bool runDailyChecks;
    bool testMode;
    TimerHandle_t alarmShortTimer;

  private:
    WaterSensorSample registeredWaterSensors[REGISTERED_WATER_SENSOR_COUNT];
    QueueHandle_t sensorSampleQueue; 
    QueueHandle_t httpRequestQueue;
    WiFiReceiverTask* wifiReceiverTask;
    WiFiTransmitterTask* wiFiTransmitterTask;
    XBeeTask* xbeeTask;
    uint32_t ipAddress;
    uint32_t getRemainingHeapSize();
    bool waitForWiFiAssociation();
    bool getIpAddress();
    bool waitForXbeeToFormNetwork();
    bool isWetValue;
    char* getWetSensorName();
    void setBlueSwitchLedState(bool state, bool isFromIsr);
    TimerHandle_t blueSwitchLedTimer;
    TimerHandle_t dailyChecksTimer;
    TimerHandle_t testModeTimer;
    TimerHandle_t alarmLongTimer;
    bool checkHealth(char* buffer);

    WaterSensorSample* getRegisteredWaterSensor(uint64_t address);
    WaterSensorSample* updateWaterSensorRegistration(WaterSensorSample* sample);


    WaterSensorSample* getSensorWithLowestBattery();
    WaterSensorSample* getSensorWithLowestRssi();
    WaterSensorSample* getSensorWithLongestSleep();
    uint8_t getActiveWaterSensorCount();
    void updateLcd(char* clockBuffer, Font* largeFont, Font* medFont, Font* smallFont, Font* xSmallFont, bool isHealthy);
    void updateLcdMainPage(char* clockBuffer, Font* largeFont, Font* medFont, Font* smallFont, bool isHealthy);
    void updateLcdSensorList(char* clockBuffer, Font* font, Font* xSmallFont, uint8_t pageNumber);
    void createHttpResponse(HttpPacket* packet);
    void createHttpStatusResponse(HttpPacket* packet);
    void createHttpHeapStatusResponse(HttpPacket* packet);
    void createHttpDisplayResponse(HttpPacket* packet);
    void createHttpResetResponse(HttpPacket* packet, char* resource);
    void createHttpOverrideResponse(HttpPacket* packet, char* resource);
    void createHttpRootUsageResponse(HttpPacket* packet);
    void createHttpTestModeResponse(HttpPacket* packet);

    bool isValidHeapAddress(uint8_t* address);
};


#endif