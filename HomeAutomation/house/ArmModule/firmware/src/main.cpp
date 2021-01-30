#include <main.h>

bool runUnitTests() {
    size_t startingFreeHeapSize = xPortGetFreeHeapSize();
    TestRunner_start();
    TestRunner_runTest(WiFiTask_test());
    TestRunner_end();
    size_t endingFreeHeapSize = xPortGetFreeHeapSize();
    if (isDebugPrintfEnabled()) debug_printf("%i byes of heap memory growth during tests" NEWLINE, startingFreeHeapSize-endingFreeHeapSize);


    TestResult testResult = TestRunner_getResult();
    return (testResult.failureCount == 0);
}

bool runPeripheralTests() {
    return true;
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

    SystemCoreClockUpdate();
}

int main(void) {
    initPll();
    watchDogResetFlag = RCC_GetFlagStatus(RCC_FLAG_IWDGRST);
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


    LEDs* leds = new LEDs();
    leds->init();
    leds->allOff();

    bool peripheralPass = (watchDogResetFlag == SET) || runPeripheralTests();
    bool postPass = unitPass & peripheralPass;

    if (watchDogResetFlag == RESET) {

        if (postPass) {
            printf(NEWLINE "POST Passed." NEWLINE);
            leds->setGreenState(true);
        } else {
            printf(NEWLINE "There were test failures" NEWLINE "HALTED" NEWLINE);
            leds->setRedState(true);
            while(true) {
                leds->setRedState(true);  // Any line - Allows a breakpoint top be set.
            }
        }
    }


    if (postPass) {
        WiFiTask* wifiTask = new WiFiTask();
        wifiTask->init();
        wifiTask->startTask("WiFi" /* task name */, 2 /* priority */, 2048 /* stack depth */);

        vTaskStartScheduler(); // blocking call
    } else {
    }


    debug_exit(0);
    return 0;
}

