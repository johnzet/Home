#ifndef MAIN_TASK_H
#define MAIN_TASK_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <CommonConfig.h>
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

extern LEDs* leds;
extern Clock* clock;
extern LCD* lcd;
extern BlockLink_t *osAllocatedHeapStart;
extern MessageList* messageList;


#pragma GCC diagnostic ignored "-Wunused-parameter"



class MainTask : public TaskClass {
  public:
     MainTask(QueueHandle_t httpRequestQueue, WiFiReceiverTask* wiFiReceiverTask, WiFiTransmitterTask* wiFiTransmitterTask);
     ~MainTask();
    virtual void init() = 0;
    virtual bool setup() = 0;
    virtual void task() = 0;

  protected: 
    QueueHandle_t httpRequestQueue;
    WiFiReceiverTask* wifiReceiverTask;
    WiFiTransmitterTask* wiFiTransmitterTask;
    bool getTimeFromServerByHttp();
    void createHttpResponse(HttpPacket* packet);
    bool waitForWiFiAssociation();
    bool getIpAddress();

  private:
    uint32_t ipAddress;

    bool isValidHeapAddress(uint8_t* address);
    uint32_t getRemainingHeapSize();

    virtual bool checkHealth(char* buffer) = 0;
    virtual void updateLcd(char* clockBuffer, Font* largeFont, Font* medFont, Font* smallFont, Font* xSmallFont, bool isHealthy) = 0;
    void createHttpStatusResponse(HttpPacket* packet);
    void createHttpHeapStatusResponse(HttpPacket* packet);
    void createHttpDisplayResponse(HttpPacket* packet);
    void createHttpRootUsageResponse(HttpPacket* packet);
    virtual void createSpecializedRootUsageResponse(Zstring* msg) = 0;
    virtual bool createSpecializedHttpResponse(HttpPacket* packet, char* resource) = 0;
    virtual void createSpecializedHttpStatusResponse(HttpPacket* packet, Zstring* msg, char* buffer) = 0;
};

#endif