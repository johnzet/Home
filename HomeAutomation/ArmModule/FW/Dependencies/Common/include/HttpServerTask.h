#ifndef HTTP_SERVER_TASK_H
#define HTTP_SERVER_TASK_H

#include <MainTask.h>

class HttpServerTask : public TaskClass {
     HttpServerTask(QueueHandle_t httpRequestQueue, WiFiReceiverTask* wiFiReceiverTask, WiFiTransmitterTask* wiFiTransmitterTask);
     ~HttpServerTask();
    void init();
    void task();

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
