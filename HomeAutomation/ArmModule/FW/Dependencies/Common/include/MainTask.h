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
     MainTask();
     ~MainTask();
    virtual void init() = 0;
    virtual bool setup() = 0;
    virtual void task() = 0;

};

#endif