#include <SpecializedMain.h>
#include <SpecializedMainTask.h>

uint8_t getSpecializedTaskCount() {
    return 2;
}

TaskClass** getSpecializedTasks(QueueHandle_t httpRequestQueue, WiFiReceiverTask* wiFiReceiverTask, WiFiTransmitterTask* wiFiTransmitterTask) {
    TaskClass** tasks = new TaskClass*[2];

    WaterValve* waterValve;
    waterValve = new WaterValve();
    pvPortSetTypeName(waterValve, "WtrVlve");
    waterValve->init();

    QueueHandle_t sensorSampleQueue;
    sensorSampleQueue = xQueueCreate(10, sizeof(WaterSensorSample));
    pvPortSetTypeName(sensorSampleQueue, "SensSmpQ");

    XBeeTask* xbeeTask;
    MainTask* mainTask;

    xbeeTask = new XBeeTask(sensorSampleQueue);
    mainTask = new SpecializedMainTask(sensorSampleQueue, httpRequestQueue, wiFiReceiverTask, wiFiTransmitterTask, xbeeTask, waterValve);

    pvPortSetTypeName(xbeeTask, "XbeeTask");
    pvPortSetTypeName(mainTask, "MainTask");

    xbeeTask->init();
    xbeeTask->startTask("XBee" /* task name */, 3 /* priority */, 512 /* stack depth */);

    mainTask->init();
    mainTask->startTask("Main" /* task name */, 3 /* priority */, 3000 /* stack depth */);

    tasks[0] = xbeeTask;
    tasks[1] = mainTask;

    return tasks;
}

const char* getModuleName() {
    return MODULE_NAME;
}

const char* getFirmwareVersion() {
    return FW_VERSION;
}
