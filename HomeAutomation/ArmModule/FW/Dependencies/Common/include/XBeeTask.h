#ifndef XBEE_H
#define XBEE_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <task.h>
#include <queue.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <TaskWrapper.h>
#include <HttpPacket.h>
#include <Zstring.h>
#include <SysMonitorTask.h>
#include <LEDs.h>
#include <AtCmdPacket.h>
#include <CLock.h>
#include <WaterSensorSample.h>
#include <MessageList.h>

#define NEWLINE "\r\n"

extern LEDs* leds;
extern Clock* clock;
extern MessageList* messageList;

class XBeeTask : public TaskClass {
  public:
    explicit XBeeTask(QueueHandle_t sampleQueue);
    ~XBeeTask();
    static void initXBeeUsart();
    void init();
    void task();
    void irqHandler();
    void sendAtCmd(AtCmdPacket* packet);
    void sendRemoteAtCmd(RemoteAtCmdPacket* packet);
    bool getAtCmdResponsePacket(AtCmdPacket* packet);
    bool atCmdResponseReceived = false;
    void requestName(WaterSensorSample* sample);
    void requestRssi(WaterSensorSample* sample);


  private:
    QueueHandle_t usartQueue;
    QueueHandle_t sampleQueue;
    bool escapeChar(char* c); // escape char is 0x7D
    char unEscapeChar(char c); // escape char is 0x7D
    AtCmdPacket* atCmdRxPacket;

    // Tx
    void xbeeWriteBuffer(Zstring* buffer);
    
    // Rx
    void loadPacketByte(uint8_t c);
    void loadDataSamplePacketByte(uint8_t c, bool isFirstByte, bool isLastByte);
    void loadNodeIdPacketByte(uint8_t c, bool isFirstByte, bool isLastByte);
    void loadAtCmdResponsePacketByte(uint8_t c, bool isFirstByte, bool isLastByte);
    void loadRemoteAtCmdResponsePacketByte(uint8_t c, bool isFirstByte, bool isLastByte);

//    friend void test_pid_params();
//    friend void test_feedback_params();
//    friend void test_properties();
};


#endif