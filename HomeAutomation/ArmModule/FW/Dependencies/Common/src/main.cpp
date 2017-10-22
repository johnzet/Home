#include <main.h>

MessageList* messageList;
LEDs* leds;  
Clock* clock;


QueueHandle_t httpRequestQueue;


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

void initWindowWatchdog() {
    RCC_APB1PeriphClockCmd(RCC_APB1Periph_WWDG, ENABLE);

    WWDG_SetPrescaler(WWDG_Prescaler_8);    
    WWDG_SetCounter(80);
    WWDG_SetWindowValue(80);
    WWDG_Enable(127);
    WWDG_ClearFlag();
    WWDG_EnableIT();
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
    watchDogResetFlag = RCC_GetFlagStatus(RCC_FLAG_IWDGRST);
    RCC_ClearFlag();
 
    initPll();
 
    initWatchdog();    
    DBGMCU->APB1FZ |= DBGMCU_IWDG_STOP;
 
    bool unitPass = runUnitTests();
//    testResetHeapState();

     NVIC_PriorityGroupConfig( NVIC_PriorityGroup_4 );  //  http://www.freertos.org/RTOS-Cortex-M3-M4.html
     // Stm32F4 interrupt priorities in FreeRtos range from 0x80 to 0xF0.  Subpriority is 0 or 1.
 
    clock = new Clock();
    messageList = new MessageList();
 
    char buffer[80];
    sprintf(buffer, "%s Firmware Version %s", getModuleName (), getFirmwareVersion ()); messageList->addMessage(buffer);
    sprintf(buffer, "HSI_VALUE = %f MHz", HSI_VALUE/1000000.0);   messageList->addMessage(buffer);
    sprintf(buffer, "SYSCLK = %f MHz", sysclk/1000000.0);   messageList->addMessage(buffer);
    sprintf(buffer, "HCLK = %f MHz", hclk/1000000.0);   messageList->addMessage(buffer);
    sprintf(buffer, "PCLK1 = %f MHz", pclk1/1000000.0);   messageList->addMessage(buffer);
    sprintf(buffer, "PCLK2 = %f MHz", pclk2/1000000.0);   messageList->addMessage(buffer);
    if (watchDogResetFlag == SET) messageList->addMessage("FIRST RUN FOLLOWING A WATCHDOG RESET");
 
    leds = new LEDs();  
 
    httpRequestQueue = xQueueCreate(10, 4);
    QueueHandle_t transmissionStatusQueue = xQueueCreate(1, sizeof(bool));
    assert(transmissionStatusQueue != NULL);
 
    WiFiReceiverTask* wiFiReceiverTask = new WiFiReceiverTask(httpRequestQueue, transmissionStatusQueue);
    WiFiTransmitterTask* wiFiTransmitterTask = new WiFiTransmitterTask(messageList, transmissionStatusQueue);

    lcd = new LCD();


    pvPortSetTypeName(leds, "LEDs");
    pvPortSetTypeName(clock, "Clock");
    pvPortSetTypeName(wiFiReceiverTask, "WiFiRcvr");
    pvPortSetTypeName(wiFiTransmitterTask, "WiFiXmtr");


    leds->init();
    leds->allOff();
    if (watchDogResetFlag == SET) leds->setRedState(true);
    lcd->init();


    bool peripheralPass = runPeripheralTests();
    bool postPass = unitPass & peripheralPass;

    

    if (postPass) {
        messageList->addMessage("POST Passed.");
        leds->setGreenState(true);
    } else {
        messageList->addMessage("There were test failures.");
        messageList->addMessage("HALTED");
        leds->setRedState(true);
        while(true) {
            leds->setRedState(true);  // Any line - Allows a breakpoint top be set.
        }
    }

    
    clock->init();

    // 0 = lowest priority, highest priority is (configMAX_PRIORITIES -1)
    if (postPass) {       
        wiFiReceiverTask->init();
        uint8_t count = 1;
        wiFiReceiverTask->startTask("WiFiRcvr" /* task name */, 3 /* priority */, 512 /* stack depth */);

        wiFiTransmitterTask->init();
        count++;
        wiFiTransmitterTask->startTask("WifiXmtr" /* task name */, 3 /* priority */, 512 /* stack depth */);

        uint8_t specializedTaskCount = getSpecializedTaskCount();
        TaskClass** specializedTasks = getSpecializedTasks(httpRequestQueue, wiFiReceiverTask, wiFiTransmitterTask);

        TaskClass* tasks[count + specializedTaskCount] = {wiFiReceiverTask, wiFiTransmitterTask};
        for (int i=0; i<specializedTaskCount; i++) {
            tasks[count+i] = specializedTasks[i];
        }
        SysMonitorTask* sysMonitorTask = new SysMonitorTask(tasks, count + specializedTaskCount);
        sysMonitorTask->init();
        sysMonitorTask->startTask("SysMon", 3, 512);

        checkStackLevel();
        vTaskStartScheduler(); // blocking call
    } else {
    }


    debug_exit(0);
    return 0;
}

