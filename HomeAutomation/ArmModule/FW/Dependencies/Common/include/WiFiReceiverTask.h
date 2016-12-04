#ifndef WIFI_H
#define WIFI_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <task.h>
#include <queue.h>
#include <portable.h>
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
#include <MessageList.h>

#define NEWLINE "\r\n"
#define BUFFER_LENGTH 2000

extern LEDs* leds;
extern Clock* clock;
extern MessageList* messageList;

class WiFiReceiverTask : public TaskClass {
  public:
    explicit WiFiReceiverTask(QueueHandle_t httpRequestQueue, QueueHandle_t transmissionStatusQueue);
    ~WiFiReceiverTask();
    static void initWiFiUsart();
    bool modemHasJoined;
    void init();
    void task();
    void irqHandler();

    // Rx Request

    // Rx Response
    void startChunkedIpv4Response(HttpPacket* packet);
    void sendIpv4ResponseChunk(HttpPacket* packet, Zstring* chunk);
    void endChunkedIpv4Response(HttpPacket* packet);

    // Tx Request
    void sendAtCmd(AtCmdPacket* packet);
    void sendIpv4TxRequestPacket(HttpPacket* packet, char* resource);

    // Tx Response
    bool getAtCmdResponsePacket(AtCmdPacket* packet);
    HttpPacket* getIpv4TxResponsePacket();







  private:
    QueueHandle_t usartQueue;
    QueueHandle_t httpRequestQueue;
    QueueHandle_t transmissionStatusQueue;
    char workingBuffer[BUFFER_LENGTH];
    bool httpRequestReceived;
    bool httpResponseReceived;
    HttpPacket* packet;
    HttpPacket* responsePacket;
    AtCmdPacket* atCmdRxPacket;
    uint8_t modemStatus;
    void formatIpAddress(char* buffer, uint32_t address);
    bool escapeChar(char* c); // escape char is 0x7D
    char unEscapeChar(char c); // escape char is 0x7D

    // Rx Request
    void loadModemStatusPacketByte(uint8_t c, bool isFirstByte, bool isLastByte);
    void handleHttpRequest(HttpPacket* p);

    // Tx Request
    void addIpv4TxRequestHeaderToPayload(HttpPacket* packet, char* resource);

    // Tx Response
    void loadAtCmdResponsePacketByte(uint8_t c, bool isFirstByte, bool isLastByte);
    void loadTransmissionStatusPacketByte(uint8_t c, bool isFirstByte, bool isLastByte);
    
    // Tx
    Zstring* assembleIpV4TxPacket(HttpPacket* packet);
    bool wifiWriteBuffer(Zstring* buffer);
    
    // Rx
    void loadPacketByte(uint8_t c);
    void loadIpV4PacketByte(uint8_t c, bool isFirstByte, bool isLastByte);




    friend void test_WiFiParseRequest();
    friend void test_WiFiStatusResponse();
//    friend void test_pid_params();
//    friend void test_feedback_params();
//    friend void test_properties();
};


#endif