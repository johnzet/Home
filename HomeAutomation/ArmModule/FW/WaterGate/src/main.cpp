#include <main.h>

MessageList* messageList;
LEDs* leds;  
Clock* clock;

WaterValve* waterValve;

QueueHandle_t sensorSampleQueue;
QueueHandle_t httpRequestQueue;

XBeeTask* xbeeTask;
MainTask* mainTask;

LCD *lcd;



extern "C" void testResetHeapState();

bool runUnitTests() {
    TestRunner_start();
    TestRunner_runTest(Heap_test());
    TestRunner_runTest(Zstring_test());
    TestRunner_runTest(HttpPacket_test());
    //TestRunner_runTest(WiFiTask_test());
    TestRunner_end();


    TestResult testResult = TestRunner_getResult();
    return (testResult.failureCount == 0);
}

bool runPeripheralTests() {

    return true;
}

void initWatchdog() {
    RCC_LSICmd( ENABLE );
    while(IWDG_GetFlagStatus(IWDG_FLAG_PVU) && IWDG_GetFlagStatus(IWDG_FLAG_RVU));
    IWDG_WriteAccessCmd(IWDG_WriteAccess_Enable);
    // prescaler or 256 and reload value of 4000 gives about 30s window
    IWDG_SetPrescaler(IWDG_Prescaler_256);
    IWDG_SetReload(4000);
    IWDG_ReloadCounter();
    IWDG_WriteAccessCmd(IWDG_WriteAccess_Disable);

    IWDG_Enable();
}

void initPll() {
    uint32_t control = __get_CONTROL();
    __set_CONTROL(control | 0b100); // enable the FPU and privileged mode

    PWR_OverDriveCmd(ENABLE);
    PWR_OverDriveSWCmd(ENABLE);

    RCC_HSEConfig(RCC_HSE_ON);
    RCC_PLLConfig(RCC_PLLSource_HSE, 4, 360, 4, 15);

    // switch to PLL clock as system clock
    RCC_SYSCLKConfig(RCC_SYSCLKSource_PLLCLK);
    RCC_HCLKConfig(RCC_SYSCLK_Div1);
    RCC_PCLK1Config(RCC_HCLK_Div4);
    RCC_PCLK2Config(RCC_HCLK_Div2);

    FLASH_DataCacheReset();
    FLASH_InstructionCacheReset();
    FLASH_DataCacheCmd(ENABLE);
    FLASH_InstructionCacheCmd(ENABLE);
    FLASH_PrefetchBufferCmd(ENABLE);
    FLASH_SetLatency(FLASH_Latency_5);

    RCC_PLLCmd(ENABLE);
    while(RCC_GetFlagStatus(RCC_FLAG_PLLRDY) == 0) {}

    while(RCC_GetSYSCLKSource() != 0x08) {}

    RCC_ClocksTypeDef RCC_ClocksStatus;
    RCC_GetClocksFreq(&RCC_ClocksStatus);
    sysclk = RCC_ClocksStatus.SYSCLK_Frequency;
    hclk = RCC_ClocksStatus.HCLK_Frequency;
    pclk1 = RCC_ClocksStatus.PCLK1_Frequency;
    pclk2 = RCC_ClocksStatus.PCLK2_Frequency;

    //SystemCoreClockUpdate();
    SystemCoreClock = 180000000;
}


int main(void) {
    initPll();

    watchDogResetFlag = RCC_GetFlagStatus(RCC_FLAG_IWDGRST);
    initWatchdog();
    
    DBGMCU->APB1FZ |= DBGMCU_IWDG_STOP;

    RCC_ClearFlag();
    NVIC_PriorityGroupConfig( NVIC_PriorityGroup_4 );  //  http://www.freertos.org/RTOS-Cortex-M3-M4.html
    // Stm32F4 interrupt priorities in FreeRtos range from 0x80 to 0xF0.  Subpriority is 0 or 1.

//    printf("\033[2J\033[H\033[0m");  // clear terminal screen
//    printf("Otto Version 0.1" NEWLINE);
//    printf("HSI_VALUE = %f MHz" NEWLINE, HSI_VALUE/1000000.0);
//    printf("SYSCLK clock rate = %f MHz" NEWLINE, sysclk/1000000.0);
//    printf("HCLK   clock rate = %f MHz" NEWLINE, hclk/1000000.0);
//    printf("PCLK1  clock rate = %f MHz" NEWLINE, pclk1/1000000.0);
//    printf("PCLK2  clock rate = %f MHz" NEWLINE, pclk2/1000000.0);
//    printf(NEWLINE "Starting POST" NEWLINE);


    bool unitPass = (watchDogResetFlag == SET) || runUnitTests();
//    testResetHeapState();

    messageList = new MessageList();
    leds = new LEDs();  
    clock = new Clock();

    waterValve = new WaterValve();

    sensorSampleQueue = xQueueCreate(10, sizeof(WaterSensorSample));
    httpRequestQueue = xQueueCreate(2, 4);
    QueueHandle_t transmissionStatusQueue = xQueueCreate(1, sizeof(bool));
    assert(transmissionStatusQueue != NULL);

    WiFiReceiverTask* wiFiReceiverTask = new WiFiReceiverTask(httpRequestQueue, transmissionStatusQueue);
    WiFiTransmitterTask* wiFiTransmitterTask = new WiFiTransmitterTask(messageList, transmissionStatusQueue);
    xbeeTask = new XBeeTask(sensorSampleQueue);
    mainTask = new MainTask(sensorSampleQueue, httpRequestQueue, wiFiReceiverTask, wiFiTransmitterTask, xbeeTask, waterValve);

    lcd = new LCD();


    pvPortSetTypeName(leds, "LEDs");
    pvPortSetTypeName(clock, "Clock");
    pvPortSetTypeName(waterValve, "WtrVlve");
    pvPortSetTypeName(wiFiReceiverTask, "WiFiRcvr");
    pvPortSetTypeName(wiFiTransmitterTask, "WiFiXmtr");
    pvPortSetTypeName(xbeeTask, "XbeeTask");
    pvPortSetTypeName(mainTask, "MainTask");
    pvPortSetTypeName(sensorSampleQueue, "SensSmpQ");


    leds->init();
    leds->allOff();
    lcd->init();
    waterValve->init();


    bool peripheralPass = (watchDogResetFlag == SET) || runPeripheralTests();
    bool postPass = unitPass & peripheralPass;

    if (watchDogResetFlag == RESET) {

        if (postPass) {
            printf(NEWLINE "POST Passed." NEWLINE);
            leds->setGreenState(true);
        } else {
            printf(NEWLINE "There were test failures." NEWLINE "HALTED" NEWLINE);
            messageList->addMessage("There were test failures.");
            leds->setRedState(true);
            while(true) {
                leds->setRedState(true);  // Any line - Allows a breakpoint top be set.
            }
        }
    }
    
    clock->init();

    if (postPass) {       
        wiFiReceiverTask->init();
        wiFiReceiverTask->startTask("WiFiRcvr" /* task name */, 3 /* priority */, 512 /* stack depth */);

        wiFiTransmitterTask->init();
        wiFiTransmitterTask->startTask("WifiXmtr" /* task name */, 4 /* priority */, 512 /* stack depth */);

        xbeeTask->init();
        xbeeTask->startTask("XBee" /* task name */, 3 /* priority */, 512 /* stack depth */);

        mainTask->init();
        mainTask->startTask("Main" /* task name */, 2 /* priority */, 3000 /* stack depth */);

        const uint8_t count = 4;
        TaskClass* tasks[count] = {wiFiReceiverTask, wiFiTransmitterTask, xbeeTask, mainTask};
        SysMonitorTask* sysMonitorTask = new SysMonitorTask(tasks, count);
        sysMonitorTask->init();
        sysMonitorTask->startTask("SysMon", 4, 512);

        checkStackLevel();
        vTaskStartScheduler(); // blocking call
    } else {
    }


    debug_exit(0);
    return 0;
}

