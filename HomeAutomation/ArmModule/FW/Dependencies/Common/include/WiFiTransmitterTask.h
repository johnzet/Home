#ifndef WiFiTransmitter_H
#define WiFiTransmitter_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <queue.h>
#include <task.h>
#include <AtCmdPacket.h>
#include <HttpPacket.h>
#include <TaskWrapper.h>
#include <MessageList.h>

#pragma GCC diagnostic ignored "-Wwrite-strings"

class WiFiTransmitterTask : public TaskClass {

    public:
        WiFiTransmitterTask(MessageList* messageList, QueueHandle_t transmissionStatusQueue);
        ~WiFiTransmitterTask();
        void init();
        void task();
        void sendAtCmd(AtCmdPacket* packet);
        void sendIpv4TxRequestPacket(HttpPacket* packet, char* resource);

        void startChunkedIpv4Response(HttpPacket* packet);
        void sendIpv4ResponseChunk(HttpPacket* packet, Zstring* chunk);
        void endChunkedIpv4Response(HttpPacket* packet);

    private:
        void formatIpAddress(char* buffer, uint32_t address);
        QueueHandle_t transmissionStatusQueue;
        MessageList* messageList;
        void addIpv4TxRequestHeaderToPayload(HttpPacket* packet, char* resource);
        Zstring* assembleIpV4TxPacket(HttpPacket* packet);
        bool wifiWriteBuffer(Zstring* buffer);
};

#endif