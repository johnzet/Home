#ifndef WIFI_H
#define WIFI_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <task.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <TaskWrapper.h>

#define NEWLINE "\n\r"
#define BUFFER_LENGTH 2000


class WiFiTask : public TaskClass {
  public:
    WiFiTask();
    static void initWiFiUsart();
    void init();
    void task();
    void irqHandler();

  private:
    
    char buffer[BUFFER_LENGTH];
    char staleBuffer[BUFFER_LENGTH];
    char httpRequestBuffer[BUFFER_LENGTH];
    bool httpRequestReceived;
    void wifiReplyOk(char* msg);
    void wifiWriteBuffer(char *b);
    void loadPacketByte(uint8_t c);
    void handleHttpRequest(char* requestBuffer);

    friend void test_WiFiParseRequest();
//    friend void test_pid_params();
//    friend void test_feedback_params();
//    friend void test_properties();
};


#endif