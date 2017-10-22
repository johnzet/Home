#ifndef WATER_GATE_MAIN_TASK_H
#define WATER_GATE_MAIN_TASK_H

#include <MainTask.h>

#include <XBeeTask.h>
#include <WaterValve.h>

#define PIR_PIN         GPIO_Pin_3
#define SWITCH_LED_PIN  GPIO_Pin_10
#define SWITCH_PIN      GPIO_Pin_11

#define REGISTERED_WATER_SENSOR_COUNT 18


class SpecializedMainTask : public MainTask {
  public:
    SpecializedMainTask(QueueHandle_t sensorSampleQ, QueueHandle_t httpRequestQueue, 
        WiFiReceiverTask* wiFiReceiverTask, WiFiTransmitterTask* wiFiTransmitterTask, 
        XBeeTask* xbeeTask, WaterValve* valve) ;
    ~SpecializedMainTask();
    WaterValve* waterValve;
    void init();
    bool setup();
    void task();
    void handlePirIrq();
    void handleButtonIrq();
    void blueLedCallback(TimerHandle_t timer);
    void dailyChecksCallback(TimerHandle_t timer);
    void alarmShortTimerCallback(TimerHandle_t timer);
    void alarmLongTimerCallback(TimerHandle_t timer);
    void testModeStopCallback(TimerHandle_t timer);


  private:
    QueueHandle_t sensorSampleQ;
    bool testMode;
    WaterSensorSample registeredWaterSensors[REGISTERED_WATER_SENSOR_COUNT];
    XBeeTask* xbeeTask;
    bool isWetValue;
    char* getWetSensorName();
    TimerHandle_t blueSwitchLedTimer;
    TimerHandle_t dailyChecksTimer;
    TimerHandle_t testModeTimer;
    TimerHandle_t alarmShortTimer;
    TimerHandle_t alarmLongTimer;

    bool waitForXbeeToFormNetwork();
    void setBlueSwitchLedState(bool state, bool isFromIsr);
    bool checkHealth(char* buffer);
    char* getOverallStatusMessage();

    bool isWet();
    void silenceAlarm();
    void resetState(bool fromIsr);
    uint8_t lcdPageNumber;
    bool runDailyChecks;
    void setIsWet(bool isWet);
    WaterSensorSample* getRegisteredWaterSensor(uint64_t address);
    WaterSensorSample* updateWaterSensorRegistration(WaterSensorSample* sample);
    WaterSensorSample* getSensorWithLowestBattery();
    WaterSensorSample* getSensorWithLowestRssi();
    WaterSensorSample* getSensorWithLongestSleep();
    uint8_t getActiveWaterSensorCount();

    void updateLcd(char* buffer, Font* largeFont, Font* medFont, Font* smallFont, Font* xSmallFont, bool isHealthy);
    void updateLcdMainPage(char* clockBuffer, Font* largeFont, Font* medFont, Font* smallFont, bool isHealthy);
    void updateLcdSensorList(char* clockBuffer, Font* font, Font* xSmallFont, uint8_t pageNumber);

    bool createSpecializedHttpResponse (HttpPacket* packet, char* resource);
    void createSpecializedHttpStatusResponse(HttpPacket* packet, Zstring* msg, char* buffer);
    void createHttpResetResponse(HttpPacket* packet, char* resource);
    void createHttpOverrideResponse(HttpPacket* packet, char* resource);
    void createSpecializedRootUsageResponse(Zstring* msg);
    void createHttpTestModeResponse(HttpPacket* packet);
};

extern "C" {
    SpecializedMainTask* irqMainTaskObject;
}




#endif