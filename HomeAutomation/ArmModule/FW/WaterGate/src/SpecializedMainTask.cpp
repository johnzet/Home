#include <SpecializedMainTask.h>

SpecializedMainTask::SpecializedMainTask(QueueHandle_t sensorSampleQ, QueueHandle_t httpRequestQueue, 
            WiFiReceiverTask* wiFiReceiverTask, WiFiTransmitterTask* wiFiTransmitterTask, 
            XBeeTask* xbeeTask, WaterValve* valve) 
        : MainTask(httpRequestQueue, wiFiReceiverTask, wiFiTransmitterTask) {

    irqMainTaskObject = this;
    this->isWetValue = false;
    this->xbeeTask = xbeeTask;
    this->waterValve = valve;

    this->sensorSampleQ = sensorSampleQ;

    WaterSensorSample nullSample = WaterSensorSample();
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        memcpy((void *)&this->registeredWaterSensors[i], (const void *)&nullSample, sizeof(nullSample));
    }
    runDailyChecks = false;
    testMode = false;
    lcdPageNumber = 0;
}

SpecializedMainTask::~SpecializedMainTask() {
}

void SpecializedMainTask::handlePirIrq() {
    if (isWet()) {
        waterValve->silenceAlarm();
    }
}

// PIR sensor
extern "C" void EXTI3_IRQHandler() {
    if (EXTI_GetITStatus(EXTI_Line3) != RESET) {
        EXTI_ClearITPendingBit(EXTI_Line3);
        irqMainTaskObject->handlePirIrq();
        xTimerStartFromISR(lcd->backlightTimerHandle, 0);
        GPIO_ResetBits(BACKLIGHT_ENABLE_PORT, BACKLIGHT_ENABLE_PIN);
    }
}

void SpecializedMainTask::handleButtonIrq() {
    if (isWet()) {
        waterValve->silenceAlarm();
        resetState(true);
    } else {
        lcdPageNumber++;
        if (lcdPageNumber > 4) lcdPageNumber = 0;
    }
}

// Front panel switch
extern "C" void EXTI15_10_IRQHandler() {
    static TickType_t ticks = 0; 
    if (EXTI_GetITStatus(EXTI_Line11) != RESET) {
        EXTI_ClearITPendingBit(EXTI_Line11);
        TickType_t ticksNow = xTaskGetTickCountFromISR();
        if ((ticksNow - ticks)/portTICK_PERIOD_MS < 500) return;
        ticks = ticksNow; 
        irqMainTaskObject->handleButtonIrq();
        GPIO_ResetBits(BACKLIGHT_ENABLE_PORT, BACKLIGHT_ENABLE_PIN);
        xTimerStartFromISR(lcd->backlightTimerHandle, 0);
    }
}

void SpecializedMainTask::blueLedCallback(TimerHandle_t timer) {
    uint8_t state = GPIO_ReadOutputDataBit(GPIOC, SWITCH_LED_PIN);
    if (state == 0) {
        GPIO_SetBits(GPIOC, SWITCH_LED_PIN);
    } else {
        GPIO_ResetBits(GPIOC, SWITCH_LED_PIN);
    }
}

void SpecializedMainTask::dailyChecksCallback(TimerHandle_t timer) {
    runDailyChecks = true;
}

void SpecializedMainTask::init() {

    this->isWetValue = false;

    GPIO_InitTypeDef GPIO_InitStructure;
    EXTI_InitTypeDef EXTI_InitStructure;
    NVIC_InitTypeDef NVIC_InitStructure;
    
    // PC3  - PIR sensor
    // PC10 - Swich LED
    // PC11 - Switch
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_SYSCFG, ENABLE);


    GPIO_InitStructure.GPIO_Pin = PIR_PIN | SWITCH_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IN;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_OD;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOC, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = SWITCH_LED_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOC, &GPIO_InitStructure);



    //  PIR Sensor
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOC, EXTI_PinSource3);
    EXTI_InitStructure.EXTI_Line = EXTI_Line3;
    EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;
    EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Falling;
    EXTI_InitStructure.EXTI_LineCmd = ENABLE;
    EXTI_Init(&EXTI_InitStructure);

    NVIC_InitStructure.NVIC_IRQChannel = EXTI3_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = configLOWEST_PRIORITY_INTERRUPT;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);        


    //  Switch
    SYSCFG_EXTILineConfig(EXTI_PortSourceGPIOC, EXTI_PinSource11);
    EXTI_InitStructure.EXTI_Line = EXTI_Line11;
    EXTI_InitStructure.EXTI_Mode = EXTI_Mode_Interrupt;
    EXTI_InitStructure.EXTI_Trigger = EXTI_Trigger_Falling;
    EXTI_InitStructure.EXTI_LineCmd = ENABLE;
    EXTI_Init(&EXTI_InitStructure);

    NVIC_InitStructure.NVIC_IRQChannel = EXTI15_10_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = configLOWEST_PRIORITY_INTERRUPT;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);

    MainTask::init();
}

void SpecializedMainTask::testModeStopCallback(TimerHandle_t timer) {
    testMode = false;
}

void SpecializedMainTask::alarmLongTimerCallback(TimerHandle_t timer) {
    waterValve->soundAlarm();
    xTimerStart(alarmShortTimer, 1000/portTICK_PERIOD_MS);
}

void SpecializedMainTask::alarmShortTimerCallback(TimerHandle_t timer) {
    waterValve->silenceAlarm();
}

extern "C" {
    void blueLedCallbackFp(TimerHandle_t xTimer) {
        irqMainTaskObject->blueLedCallback(xTimer);
    }

    void dailyChecksCallbackFp(TimerHandle_t xTimer) {
        irqMainTaskObject->dailyChecksCallback(xTimer);
    }
    
    void testModeStopCallbackFp(TimerHandle_t xTimer) {
        irqMainTaskObject->testModeStopCallback(xTimer);
    }

    void alarmLongTimerCallbackFp(TimerHandle_t xTimer) {
        irqMainTaskObject->alarmLongTimerCallback(xTimer);
    }

    void alarmShortTimerCallbackFp(TimerHandle_t xTimer) {
        irqMainTaskObject->alarmShortTimerCallback(xTimer);
    }
}

void SpecializedMainTask::task() {
    RTC_DateTypeDef lastRequestDate;
    RTC_TimeTypeDef lastRequestTime;
    RTC_DateStructInit(&lastRequestDate);
    RTC_TimeStructInit(&lastRequestTime);

    vTaskDelay(1000/portTICK_PERIOD_MS);  // allow hardware to set up somewhat
    
    setup();

    void* blueSwitchLedTimerId = new char[1];
    blueSwitchLedTimer = xTimerCreate(
        "Blue SW Led", 
        1000/portTICK_PERIOD_MS, 
        pdTRUE, 
        blueSwitchLedTimerId, 
        blueLedCallbackFp
    );
    xTimerStart(this->blueSwitchLedTimer, 1000/portTICK_PERIOD_MS);
    setBlueSwitchLedState(false, false);

    void* dailyChecksTimerId = new char[1];
    dailyChecksTimer = xTimerCreate(
        "Daily Checks",
        24 * 3600 * 1000/portTICK_PERIOD_MS,
        pdTRUE,
        dailyChecksTimerId,
        dailyChecksCallbackFp
    );
    xTimerStart(this->dailyChecksTimer, 1000/portTICK_PERIOD_MS);

    void* testModeTimerId = new char[1];
    testModeTimer = xTimerCreate(
        "Test Mode",
        1 * 3600 * 1000/portTICK_PERIOD_MS,
        pdFALSE,
        testModeTimerId,
        testModeStopCallbackFp
    );

    void* alarmLongTimerId = new char[1];
    alarmLongTimer = xTimerCreate(
        "Alarm Long",
        1 * 60 * 1000/portTICK_PERIOD_MS,
        pdTRUE,
        alarmLongTimerId,
        alarmLongTimerCallbackFp
    );
    
    void* alarmShortTimerId = new char[1];
    alarmShortTimer = xTimerCreate(
        "Alarm Short",
        1 * 1000/portTICK_PERIOD_MS,
        pdFALSE,
        alarmShortTimerId,
        alarmShortTimerCallbackFp
    );


    char* buffer = new char[50];
    pvPortSetTypeName(buffer, "bufferM1");
    FontGeorgia30x20 largeFont = FontGeorgia30x20();
    FontGeorgia16x10 medFont = FontGeorgia16x10();
    FontGeorgia10x7 smallFont = FontGeorgia10x7();
    FontGeorgia7x5 xSmallFont = FontGeorgia7x5();
    HttpPacket* packet;
    pvPortSetTypeName(buffer, "HttpPkM1");
    WaterSensorSample sample = WaterSensorSample();


//*********************************************************************************************************************************
    while(true) {
        if (this->heartbeatSemaphore != NULL) {
            xSemaphoreGive(this->heartbeatSemaphore);
        }
        if (this->runDailyChecks) {
            this->getTimeFromServerByHttp();
            this->runDailyChecks = false;
        }

        if (xQueueReceive(this->sensorSampleQ, &sample, 100/portTICK_PERIOD_MS) == pdPASS) {
            if (this->isWet() && this->testMode) this->setIsWet(false);

            WaterSensorSample* registeredSample = updateWaterSensorRegistration(&sample);
            
            if (registeredSample->isWet() && !this->isWet()) {
                // shut water main
                this->setIsWet(true);
                if (!testMode) {
                    this->waterValve->openBypassValve();
                    vTaskDelay(3000);  // Wait for current draw to fall
                    this->waterValve->closeValve();
                    this->waterValve->soundAlarm();
                }
                this->setBlueSwitchLedState(true, false);
            }
            if (clock->getSecondsAgo(&lastRequestDate, &lastRequestTime) > 5) {
                lastRequestDate = clock->getDate();
                lastRequestTime = clock->getTime();
                RTC_DateTypeDef sampleDate = registeredSample->getDate();
                RTC_TimeTypeDef sampleTime = registeredSample->getTime();
                if (registeredSample->getName() == NULL || strlen(registeredSample->getName()) == 0) {
                    xbeeTask->requestName(registeredSample);
                }
                if (registeredSample->getRssi() == 0 || clock->getSecondsAgo(&sampleDate, &sampleTime) >= 3600.0f*24.0f) {
                    xbeeTask->requestRssi(registeredSample);
                }
            }
        }
        
        if (xQueueReceive(this->httpRequestQueue, &packet, 100/portTICK_PERIOD_MS) == pdPASS) {
            this->createHttpResponse(packet);
            delete packet;
        }


        this->waterValve->checkState();

        bool isHealthy = this->checkHealth(buffer);

//        if (counter++ == 5) {
            updateLcd(buffer, &largeFont, &medFont, &smallFont, &xSmallFont, isHealthy);
//            counter = 0;
//        }

        if (!isHealthy) {
            if (!xTimerIsTimerActive(this->alarmLongTimer)) {
                this->waterValve->soundAlarm();
                xTimerStart(this->alarmShortTimer, 1000/portTICK_PERIOD_MS);
                xTimerStart(this->alarmLongTimer, 1000/portTICK_PERIOD_MS);
            }
        } else {
            if (xTimerIsTimerActive(this->alarmLongTimer)) {
                xTimerStop(this->alarmLongTimer, 1000/portTICK_PERIOD_MS);
                xTimerStop(this->alarmShortTimer, 1000/portTICK_PERIOD_MS);
                this->waterValve->silenceAlarm();
            }
        }

        vTaskDelay(200/portTICK_PERIOD_MS);
    }

    delete[] buffer;
//*********************************************************************************************************************************
}

void SpecializedMainTask::setIsWet(bool isWet) {
    if (!isWet) {
        for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
            WaterSensorSample* registeredSample = &this->registeredWaterSensors[i];
            if (registeredSample != NULL) {
                registeredSample->setIsWet(false);
            }
        }
    }
    this->isWetValue = isWet;
}

bool SpecializedMainTask::isWet() {
    return this->isWetValue;
}

void SpecializedMainTask::setBlueSwitchLedState(bool state, bool isFromIsr) {
    uint32_t period = (state? 150:750) / portTICK_PERIOD_MS;
    if (isFromIsr) {
        BaseType_t xHigherPriorityTaskWoken;
        xTimerChangePeriodFromISR(this->blueSwitchLedTimer, period, &xHigherPriorityTaskWoken);
        if (xHigherPriorityTaskWoken != pdFALSE) {
            taskYIELD();
        }
    } else {
        xTimerChangePeriod(this->blueSwitchLedTimer, period, 1000/portTICK_PERIOD_MS);
    }
}

bool SpecializedMainTask::checkHealth(char* buffer) {
    WaterSensorSample* lowestBattery = getSensorWithLowestBattery();
    WaterSensorSample* lowestRssi = getSensorWithLowestRssi();
    WaterSensorSample* longestSleep = getSensorWithLongestSleep();
    
    if (longestSleep != NULL) {
        RTC_DateTypeDef date = longestSleep->getDate();
        RTC_TimeTypeDef time = longestSleep->getTime();
        uint32_t seconds =  clock->getSecondsAgo(&date, &time);

        if ((seconds / 3600.0f) > 48) {
            sprintf(buffer, "Inoperative Sensor: %s", longestSleep->getName());
            return false;
        }
    }
    if (lowestBattery != NULL && lowestBattery->getBatteryVoltage() < 2.2f) {
        sprintf(buffer, "Weak Battery in %s", lowestBattery->getName());
        return false;
    }
    if (lowestRssi != NULL && lowestRssi->getRssi() > 90.0f) {
        sprintf(buffer, "Weak Signal to %s", lowestRssi->getName());
        return false;
    }
    return true;
}


void SpecializedMainTask::updateLcd(char* buffer, Font* largeFont, Font* medFont, Font* smallFont, Font* xSmallFont, bool isHealthy) {
    lcd->clear();

    if (this->lcdPageNumber == 0) {
        updateLcdMainPage(buffer, largeFont, medFont, smallFont, isHealthy);
    } else {
        updateLcdSensorList(buffer, smallFont, xSmallFont, this->lcdPageNumber);
    }
    lcd->refreshLcd();
}

void SpecializedMainTask::updateLcdMainPage(char* buffer, Font* largeFont, Font* medFont, Font* smallFont, bool isHealthy) {
    uint8_t spacing = 1;

    if (!isHealthy) {
       lcd->drawString(0, 115, buffer, smallFont, 0b1111, spacing);
    }


    clock->prettyPrint(buffer);
    uint32_t x = 0;
    uint32_t y = 10;
    uint32_t width=0;
    uint32_t height=0;
    lcd->drawString(10, 0, "Water Gate", medFont, 0b1111, spacing);
    lcd->drawString(140, 0, (char*)(isHealthy? "System is Healthy" : "System is Sick"), smallFont, 0b1111, spacing);
   // x = (lcd->getWidth() - width)/2;
    lcd->drawString(35, 15, buffer, smallFont, 0b1000, spacing);

    y = 35;
    Font* font = largeFont;
    if (this->isWet() ) {
        sprintf(buffer, "Water  in  %s", this->getWetSensorName());
    } else {
        sprintf(buffer, "Valve   is   %s", (this->waterValve->isValveOpen()? "Open" : "Closed"));
    }
    lcd->getFontMetrics(font, buffer, &width, &height, spacing);
    x = (lcd->getWidth() - width)/2;
    lcd->drawRectangle(x-1,y-1, x+width+1, y+height+1, 0b0000, true);
    lcd->drawString(x, y, buffer, font, 0b1111, spacing);
    lcd->drawRectangle(x-2,y-2, x+width+2, y+height+2, 0b0111, false);

    WaterSensorSample* lowestBattery = getSensorWithLowestBattery();
    WaterSensorSample* lowestRssi = getSensorWithLowestRssi();
    WaterSensorSample* longestSleep = getSensorWithLongestSleep();

    if (lowestBattery != NULL) {
        sprintf(buffer, "%s    %1.2fV", lowestBattery->getName(), lowestBattery->getBatteryVoltage());
        lcd->drawString(0, 60, buffer, smallFont, 0b1100, spacing);
    }
    sprintf(buffer, "%i   Active Sensors", getActiveWaterSensorCount());
    lcd->drawString(140, 60, buffer, smallFont, 0b1100, spacing);
    
    if (lowestRssi != NULL && lowestRssi->getRssi() > 0) {
        sprintf(buffer, "%s    -%idB", lowestRssi->getName(), lowestRssi->getRssi());
        lcd->drawString(0, 75, buffer, smallFont, 0b1100, spacing);
    }
    clock->getUptimeString(buffer);
    lcd->drawString(140, 75, buffer, smallFont, 0b1100, spacing);

    if (longestSleep != NULL) {
        RTC_DateTypeDef date = longestSleep->getDate();
        RTC_TimeTypeDef time = longestSleep->getTime();
        uint32_t seconds =  clock->getSecondsAgo(&date, &time);
        if (seconds > 0) {
            sprintf(buffer, "%s    %i Hours Sleeping", longestSleep->getName(), (int)floor(seconds/3600.0f));
            lcd->drawString(0, 90, buffer, smallFont, 0b1100, spacing);
        }
    }
    char* msg = getOverallStatusMessage();
    if (msg != NULL) {
        lcd->drawString(0, 110, msg, smallFont, 0b1100, spacing);
    }
}

char* SpecializedMainTask::getOverallStatusMessage() {
    return "*******";  // this method must free memory
}

void SpecializedMainTask::updateLcdSensorList(char* buffer, Font* font, Font* smallFont, uint8_t pageNumber) {

    uint8_t colOffset = 70;
    uint8_t spacing = 1;
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* registeredSample = &this->registeredWaterSensors[i];
        if (registeredSample->getAddress() == NULL) break;
        uint32_t row = 14 * (i/2) + 2;
        uint32_t col = 120 * (i%2);
        RTC_DateTypeDef date = registeredSample->getDate();
        RTC_TimeTypeDef time = registeredSample->getTime();
        uint32_t seconds =  clock->getSecondsAgo(&date, &time);

        lcd->drawString(col+2, row+2, registeredSample->getName(), smallFont, 0b1100, spacing);

        if (pageNumber == 1) {
            sprintf(buffer, "%1.2fV", registeredSample->getBatteryVoltage());
            lcd->drawString(col+colOffset, row, buffer, font, 0b1111, spacing);
        } else if (pageNumber == 2) {
            sprintf(buffer, "-%idB", registeredSample->getRssi());
            lcd->drawString(col+colOffset, row, buffer, font, 0b1111, spacing);
        } else if (pageNumber == 3) {
            sprintf(buffer, "%u Hours", seconds / 3600);
            lcd->drawString(col+colOffset, row, buffer, font, 0b1111, spacing);
        } else {
            sprintf(buffer, "%s", (registeredSample->isWet()? "WET" : "dry"));
            lcd->drawString(col+colOffset, row, buffer, font, 0b1111, spacing);
        }
    }
}


bool SpecializedMainTask::setup() {
    leds->setRedState(false);
    leds->setGreenState(false);

    uint32_t count = 0;
    while (!this->wifiReceiverTask->modemHasJoined) {
        vTaskDelay(100/portTICK_PERIOD_MS);
        count++;
        if (count > 200) {
            leds->setRedState(true);
            messageList->addMessage("WiFi modem has not joined.");
//            return false;
            break;
        }
    }

    if (!waitForWiFiAssociation()) {
        leds->setRedState(true);
        messageList->addMessage("WiFi association failed.");
        return false;
    }

    if (!getIpAddress()) {
        leds->setRedState(true);
        messageList->addMessage("get IP address failed.");
        return false;
    }

    if (!getTimeFromServerByHttp()) {
        leds->setRedState(true);
        messageList->addMessage("Get time from server failed.");
        return false;
    }

    if (!waitForXbeeToFormNetwork()) {
        leds->setRedState(true);
        messageList->addMessage("XBee failed to form a network.");
        return false;
    }

    leds->setGreenState(true);
       
    return true;
}

bool SpecializedMainTask::waitForXbeeToFormNetwork() {
    uint8_t count = 0;
    uint8_t associationStatus = 0xff;
    do {
        AtCmdPacket p = AtCmdPacket();
        p.setAtCmd("AI");
        Zstring cmdPayload = Zstring();
        p.setPayload(&cmdPayload);

        this->xbeeTask->sendAtCmd(&p);
        vTaskDelay(2/portTICK_PERIOD_MS);

        AtCmdPacket packet = AtCmdPacket();
        uint8_t loopCnt = 0;
        bool gotResponse = false;
        do {

            gotResponse = this->xbeeTask->getAtCmdResponsePacket(&packet);
            if (!gotResponse) {
                vTaskDelay(10/portTICK_PERIOD_MS);
            }
        } while(!gotResponse && loopCnt++ < 5);
        if (packet.getStatus() == 0) {
            associationStatus = ((uint8_t)packet.getPayload()->getChar(0));
        }
        if (associationStatus != 0) {
            vTaskDelay(1000/portTICK_PERIOD_MS);
        }
    } while(count++ < 20 && associationStatus != 0);


    return (associationStatus == 0);

}


WaterSensorSample* SpecializedMainTask::getRegisteredWaterSensor(uint64_t address) {
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* registeredSample = &this->registeredWaterSensors[i];
        if (registeredSample->getAddress() == address) {
            return registeredSample;
        }
    }
    return NULL;
}

char* SpecializedMainTask::getWetSensorName() {
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* registeredSample = &this->registeredWaterSensors[i];
        if (registeredSample->isWet()) {
            return registeredSample->getName();
        }
    }
    return "";
}

WaterSensorSample* SpecializedMainTask::updateWaterSensorRegistration(WaterSensorSample* sample) {
    
    WaterSensorSample *registeredWaterSensor = this->getRegisteredWaterSensor(sample->getAddress());
    if (registeredWaterSensor == NULL) {
        // find the first null entry
        for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
            WaterSensorSample* s = &this->registeredWaterSensors[i];
            assert(s != NULL);
            if (s->getAddress() == NULL) {
                registeredWaterSensor = s; 
                break;
            }
        }
    }
    assert(isHeapAddress(registeredWaterSensor));
    assert(sample->getAddress() > 0xFFFFFFFF);
    registeredWaterSensor->setAddress(sample->getAddress());
    if (sample->isWet()) registeredWaterSensor->setIsWet(true);
    if (sample->getBatteryVoltage() > 0.1) registeredWaterSensor->setBatteryVoltage(sample->getBatteryVoltage());
    if (strlen(sample->getName())) registeredWaterSensor->setName(sample->getName(), strlen(sample->getName()));
    if (sample->getRssi() != 0) registeredWaterSensor->setRssi(sample->getRssi());
    registeredWaterSensor->setDate(clock->getDate());
    registeredWaterSensor->setTime(clock->getTime());

    return registeredWaterSensor;
}

WaterSensorSample* SpecializedMainTask::getSensorWithLowestBattery() {
    float lowestBattery = 100;
    WaterSensorSample* sensor = NULL;
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* s = &this->registeredWaterSensors[i];
        if (s->getAddress() == NULL) {
            break;
        }
        float v = s->getBatteryVoltage();
        if (v > 0.1 && v < lowestBattery) {
            sensor = s;
            lowestBattery = v;
        }    
    }
    return sensor;
}

WaterSensorSample* SpecializedMainTask::getSensorWithLowestRssi() {
    float weakestRssi = 0;
    WaterSensorSample* sensor = NULL;
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* s = &this->registeredWaterSensors[i];
        if (s->getAddress() == NULL) {
            break;
        }
        uint8_t r = s->getRssi();
        if (r != 0 && r > weakestRssi) {
            sensor = s;
            weakestRssi = r;
        }    
    }
    return sensor;
}

WaterSensorSample* SpecializedMainTask::getSensorWithLongestSleep() {
    uint32_t longestSleep = 0;
    WaterSensorSample* sensor = NULL;
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* s = &this->registeredWaterSensors[i];
        if (s->getAddress() == NULL) {
            break;
        }
        RTC_DateTypeDef date = s->getDate();
        RTC_TimeTypeDef time = s->getTime();
        uint32_t r = clock->getSecondsAgo(&date, &time);
        if (r > longestSleep) {
            sensor = s;
            longestSleep = r;
        }    
    }
    return sensor;
}

uint8_t SpecializedMainTask::getActiveWaterSensorCount() {
    uint8_t count = 0;
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* s = &this->registeredWaterSensors[i];
        
        RTC_DateTypeDef date = s->getDate();
        RTC_TimeTypeDef time = s->getTime();
        if (date.RTC_Year == 0) {
            break;
        }
        if (clock->getSecondsAgo(&date, &time) < 2*24*3600) {
            count++;
        }
    }
    return count;
}

bool SpecializedMainTask::createSpecializedHttpResponse (HttpPacket* packet, char* resource) {
    if (strcmp("/reset", resource) == 0) {
       createHttpResetResponse(packet, resource);
    } else if (strncmp("/valve", resource, 6) == 0) {
       createHttpOverrideResponse(packet, resource);
    } else if (strncmp("/test", resource, 5) == 0) {
       createHttpTestModeResponse(packet);
    } else {
        return false;
    }
    return true;
}

void SpecializedMainTask::createSpecializedHttpStatusResponse(HttpPacket* packet, Zstring* msg, char* buffer) {

   
    msg->appendS("<br/><br/>Water Sensors<br/>");
    msg->appendS("<table>");
    msg->appendS("<tr><th>Name</th><th>State</th><th>Battery</th><th>Signal</th><th>Address</th><th>Hours Sleeping</th></tr>");
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
    assert(isHeapAddress((void*)(&this->registeredWaterSensors[i])));
        if (this->registeredWaterSensors[i].getAddress() != NULL) {
            msg->appendS("<tr>");
            msg->appendS("<td>");
            msg->appendS(this->registeredWaterSensors[i].getName());
            msg->appendS("</td><td>");
            msg->appendS(this->registeredWaterSensors[i].isWet()? (char*)"Wet" : (char*)"Dry");
            msg->appendS("</td><td>");
            sprintf(buffer, "%0.3fV", this->registeredWaterSensors[i].getBatteryVoltage());
            msg->appendS(buffer);
            msg->appendS("</td><td>");
            sprintf(buffer, "RSSI -%i", this->registeredWaterSensors[i].getRssi());
            msg->appendS(buffer); 
            msg->appendS("</td><td>");
            sprintf(buffer, "0x%X", (unsigned int)this->registeredWaterSensors[i].getAddress());
            msg->appendS(buffer);
            msg->appendS("</td>");
            msg->appendS("<td>");

            RTC_DateTypeDef date = this->registeredWaterSensors[i].getDate();
            RTC_TimeTypeDef time = this->registeredWaterSensors[i].getTime();
            float hoursOld = clock->getSecondsAgo(&date, &time)/3600.0f;
            msg->appendI((int)floor(hoursOld));
            msg->appendS("</td>");
            msg->appendS("</tr>");

            this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
            msg->clear();
            taskYIELD();
        }
    }
    msg->appendS("</table>");
    msg->appendS("<br/>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();
}

void SpecializedMainTask::resetState(bool fromIsr) {
    this->waterValve->silenceAlarm();
    if (!testMode) {
        this->waterValve->openValve();
        this->waterValve->closeBypassValve();
    }
    this->setIsWet(false);
    this->setBlueSwitchLedState(false, fromIsr);
}

void SpecializedMainTask::createHttpResetResponse(HttpPacket* packet, char* resource) {

    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM4");
    this->resetState(false);
    this->testMode = false;
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<body>");
    msg->appendS("<h2>System reset to dry mode.");
    msg->appendS("</h2>");
    msg->appendS("</body></html>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;
}

void SpecializedMainTask::createHttpTestModeResponse(HttpPacket* packet) {

    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM7");

    xTimerStart(this->testModeTimer, 1000/portTICK_PERIOD_MS);
    this->testMode = true;
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<body>");
    msg->appendS("<h2>System reset to test mode for 1 hour.  Exit early with the reset command.");
    msg->appendS("</h2>");
    msg->appendS("</body></html>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;
}

void SpecializedMainTask::createHttpOverrideResponse(HttpPacket* packet, char* resource) {
    bool valveOpen = (strncmp("/valve/open", resource, 11) == 0);
    bool valveClosed = (strncmp("/valve/close", resource, 12) == 0);
    if (valveOpen) {
        this->waterValve->openValve();
        this->setIsWet(false);
    }
    if (valveClosed) {
        this->waterValve->closeValve();
    }

    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM5");
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<body>");
    msg->appendS("<h2>");

    if (valveOpen) {
        msg->appendS("Valve is Opening");
    } else if (valveClosed) {
        msg->appendS("Valve is Closing");
    } else {
        msg->appendS("Usage:  /valve/open or /valve/close");
    }
    
    msg->appendS("</h2>");
    msg->appendS("</body></html>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;
}

void SpecializedMainTask::createSpecializedRootUsageResponse(Zstring* msg) {
    msg->appendS("<li>/test - start test mode for 1 hour</li>");
    msg->appendS("<li>/reset - restore dry operation</li>");
    msg->appendS("<li>/valve/[open|close]</li>");
}