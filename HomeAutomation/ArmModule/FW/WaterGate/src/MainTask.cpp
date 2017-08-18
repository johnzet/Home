#include <MainTask.h>

extern "C" {
    MainTask* irqMainTaskObject;
}

MainTask::MainTask(QueueHandle_t sensorSampleQ, QueueHandle_t httpRequestQueue, WiFiReceiverTask* wiFiReceiverTaskArg, 
            WiFiTransmitterTask* wiFiTransmitterTaskArg, XBeeTask* xbeeTask, WaterValve* valve) {
    irqMainTaskObject = this;
    this->isWetValue = false;
    this->wifiReceiverTask = wiFiReceiverTaskArg;
    this->wiFiTransmitterTask = wiFiTransmitterTaskArg;
    this->xbeeTask = xbeeTask;
    this->ipAddress = 0;
    this->sensorSampleQueue = sensorSampleQ;
    this->waterValve = valve;
    this->httpRequestQueue = httpRequestQueue;

    WaterSensorSample nullSample = WaterSensorSample();
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        memcpy((void *)&this->registeredWaterSensors[i], (const void *)&nullSample, sizeof(nullSample));
    }
    lcdPageNumber = 0;
    runDailyChecks = false;
    testMode = false;
}

MainTask::~MainTask() {
}

// PIR sensor
extern "C" void EXTI3_IRQHandler() {
    if (EXTI_GetITStatus(EXTI_Line3) != RESET) {
        EXTI_ClearITPendingBit(EXTI_Line3);
        if (irqMainTaskObject->isWet()) {
            irqMainTaskObject->waterValve->silenceAlarm();
        }
        xTimerStartFromISR(lcd->backlightTimerHandle, 0);
        GPIO_ResetBits(BACKLIGHT_ENABLE_PORT, BACKLIGHT_ENABLE_PIN);
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
        if (irqMainTaskObject->isWet()) {
            irqMainTaskObject->waterValve->silenceAlarm();
            irqMainTaskObject->resetState(true);
        } else {
            irqMainTaskObject->lcdPageNumber++;
            if (irqMainTaskObject->lcdPageNumber > 4) irqMainTaskObject->lcdPageNumber = 0;
        }
        GPIO_ResetBits(BACKLIGHT_ENABLE_PORT, BACKLIGHT_ENABLE_PIN);
        xTimerStartFromISR(lcd->backlightTimerHandle, 0);
    }
}

void MainTask::init() {
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
            
}

void blueLedCallback(TimerHandle_t timer) {
    uint8_t state = GPIO_ReadOutputDataBit(GPIOC, SWITCH_LED_PIN);
    if (state == 0) {
        GPIO_SetBits(GPIOC, SWITCH_LED_PIN);
    } else {
        GPIO_ResetBits(GPIOC, SWITCH_LED_PIN);
    }
}

void dailyChecksCallback(TimerHandle_t timer) {
    irqMainTaskObject->runDailyChecks = true;
}

void testModeStopCallback(TimerHandle_t timer) {
    irqMainTaskObject->testMode = false;
}

void alarmLongTimerCallback(TimerHandle_t timer) {
    irqMainTaskObject->waterValve->soundAlarm();
    xTimerStart(irqMainTaskObject->alarmShortTimer, 1000/portTICK_PERIOD_MS);
}

void alarmShortTimerCallback(TimerHandle_t timer) {
    irqMainTaskObject->waterValve->silenceAlarm();
}

void MainTask::task() {
    RTC_DateTypeDef lastRequestDate;
    RTC_TimeTypeDef lastRequestTime;
    RTC_DateStructInit(&lastRequestDate);
    RTC_TimeStructInit(&lastRequestTime);

    vTaskDelay(1000/portTICK_PERIOD_MS);  // allow hardware to set up somewhat
    
    setup();


    void* blueSwitchLedTimerId = new char[1];
    this->blueSwitchLedTimer = xTimerCreate(
        "Blue SW Led", 
        1000/portTICK_PERIOD_MS, 
        pdTRUE, 
        blueSwitchLedTimerId, 
        blueLedCallback
    );
    xTimerStart(this->blueSwitchLedTimer, 1000/portTICK_PERIOD_MS);
    this->setBlueSwitchLedState(false, false);



    void* dailyChecksTimerId = new char[1];
    this->dailyChecksTimer = xTimerCreate(
        "Daily Checks",
        24 * 3600 * 1000/portTICK_PERIOD_MS,
        pdTRUE,
        dailyChecksTimerId,
        dailyChecksCallback
    );
    xTimerStart(this->dailyChecksTimer, 1000/portTICK_PERIOD_MS);


    void* testModeTimerId = new char[1];
    this->testModeTimer = xTimerCreate(
        "Test Mode",
        1 * 3600 * 1000/portTICK_PERIOD_MS,
        pdFALSE,
        testModeTimerId,
        testModeStopCallback
    );

    void* alarmLongTimerId = new char[1];
    this->alarmLongTimer = xTimerCreate(
        "Alarm Long",
        1 * 60 * 1000/portTICK_PERIOD_MS,
        pdTRUE,
        alarmLongTimerId,
        alarmLongTimerCallback
    );
    
    void* alarmShortTimerId = new char[1];
    this->alarmShortTimer = xTimerCreate(
        "Alarm Short",
        1 * 1000/portTICK_PERIOD_MS,
        pdFALSE,
        alarmShortTimerId,
        alarmShortTimerCallback
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

        if (xQueueReceive(this->sensorSampleQueue, &sample, 100/portTICK_PERIOD_MS) == pdPASS) {
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

void MainTask::setIsWet(bool isWet) {
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

bool MainTask::isWet() {
    return this->isWetValue;
}

void MainTask::setBlueSwitchLedState(bool state, bool isFromIsr) {
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

bool MainTask::checkHealth(char* buffer) {
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

void MainTask::updateLcd(char* buffer, Font* largeFont, Font* medFont, Font* smallFont, Font* xSmallFont, bool isHealthy) {
    lcd->clear();

    if (this->lcdPageNumber == 0) {
        updateLcdMainPage(buffer, largeFont, medFont, smallFont, isHealthy);
    } else {
        updateLcdSensorList(buffer, smallFont, xSmallFont, this->lcdPageNumber);
    }
    lcd->refreshLcd();
}

void MainTask::updateLcdMainPage(char* buffer, Font* largeFont, Font* medFont, Font* smallFont, bool isHealthy) {
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

char* MainTask::getOverallStatusMessage() {
    return "*******";  // this method must free memory
}

void MainTask::updateLcdSensorList(char* buffer, Font* font, Font* smallFont, uint8_t pageNumber) {

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

bool MainTask::setup() {
    leds->setRedState(false);
    leds->setGreenState(false);

    uint32_t count = 0;
    while (!this->wifiReceiverTask->modemHasJoined) {
        vTaskDelay(100/portTICK_PERIOD_MS);
        count++;
        if (count > 200) {
            leds->setRedState(true);
            messageList->addMessage("WiFi modem has not joined.");
            return false;
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

bool MainTask::waitForWiFiAssociation() {
    uint8_t count = 0;
    uint8_t associationStatus = 0xff;
    do {
        AtCmdPacket p = AtCmdPacket();
        p.setAtCmd("AI");
        Zstring cmdPayload = Zstring();
        p.setPayload(&cmdPayload);

        this->wiFiTransmitterTask->sendAtCmd(&p);
        vTaskDelay(2/portTICK_PERIOD_MS);

        AtCmdPacket packet = AtCmdPacket();
        uint8_t loopCnt = 0;
        bool gotResponse = false;
        do {

            gotResponse = this->wifiReceiverTask->getAtCmdResponsePacket(&packet);
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

bool MainTask::getIpAddress() {
    uint8_t count = 0;
    this->ipAddress = 0;
    do {
        AtCmdPacket p = AtCmdPacket();
        p.setAtCmd("MY");
        Zstring cmdPayload = Zstring();
        p.setPayload(&cmdPayload);

        this->wiFiTransmitterTask->sendAtCmd(&p);
        vTaskDelay(2/portTICK_PERIOD_MS);

        AtCmdPacket packet = AtCmdPacket();
        uint8_t loopCnt = 0;
        bool gotResponse = false;
        do {

            gotResponse = this->wifiReceiverTask->getAtCmdResponsePacket(&packet);
            if (!gotResponse) {
                vTaskDelay(10/portTICK_PERIOD_MS);
            }
        } while(!gotResponse && loopCnt++ < 5);
        if (packet.getStatus() == 0) {
            this->ipAddress = ((uint8_t)packet.getPayload()->getChar(0)) << 24;
            this->ipAddress += ((uint8_t)packet.getPayload()->getChar(1)) << 16;
            this->ipAddress += ((uint8_t)packet.getPayload()->getChar(2)) << 8;
            this->ipAddress += ((uint8_t)packet.getPayload()->getChar(3));
        } else {
            vTaskDelay(1000/portTICK_PERIOD_MS);
        }
    } while(count++ < 20 && this->ipAddress < (1 << 24));

    
    return (this->ipAddress > (1 << 24));
}

bool MainTask::getTimeFromServerByHttp() {
    HttpPacket packet = HttpPacket();
    packet.setAddress(CONFIG_SERVER_IP_ADDRESS);
//    packet.setDestPort(80);
    packet.setFrameId(01);
    packet.setFrameType(0x20);
    packet.setOptions(0);
    packet.setProtcol(0x01);
    packet.setSourcePort(80);  // used as the dest port

//while(true) {
    wiFiTransmitterTask->sendIpv4TxRequestPacket(&packet, "/house/config");

    HttpPacket* p;
    uint8_t loopCnt = 0;
    do {
        vTaskDelay(100/portTICK_PERIOD_MS);
        p = this->wifiReceiverTask->getIpv4TxResponsePacket();
    } while(p == NULL && loopCnt++ < 200);

    if (p== NULL) {
        loopCnt = 0;
        do {
            vTaskDelay(100/portTICK_PERIOD_MS);
            p = this->wifiReceiverTask->getIpv4TxResponsePacket();
        } while(p == NULL && loopCnt++ < 200);
    }

    if (p == NULL) {
        return false;
    }


    
    char* payload = p->getPayload()->getStr();
    uint32_t length = p->getPayload()->size();

    payload[length-1] = NULL;   // just in case
    char* token = "\"rtcShortcut\": \"";
    char* loc = strstr(payload, token);

    if (loc != NULL) {
        uint32_t seconds=0;
        uint32_t minutes=0;
        uint32_t hours=0;
        uint32_t day=0;
        uint32_t month=0;
        uint32_t weekdayMondayIs1=0;
        uint32_t yearYY=0;

        int countParsed = sscanf(loc+strlen(token), "%u%u%u%u%u%u%u", &seconds, &minutes, &hours, &day, &month, &weekdayMondayIs1, &yearYY);
        if (countParsed == 7) {
            clock->setClock(seconds, minutes, hours, day, month, weekdayMondayIs1, yearYY);
        }
    } else {
        //delete p;
        return false;
    }
        

    //delete p;
    return true;
}

bool MainTask::waitForXbeeToFormNetwork() {
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


WaterSensorSample* MainTask::getRegisteredWaterSensor(uint64_t address) {
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* registeredSample = &this->registeredWaterSensors[i];
        if (registeredSample->getAddress() == address) {
            return registeredSample;
        }
    }
    return NULL;
}

char* MainTask::getWetSensorName() {
    for (uint8_t i=0; i<REGISTERED_WATER_SENSOR_COUNT; i++) {
        WaterSensorSample* registeredSample = &this->registeredWaterSensors[i];
        if (registeredSample->isWet()) {
            return registeredSample->getName();
        }
    }
    return "";
}

WaterSensorSample* MainTask::updateWaterSensorRegistration(WaterSensorSample* sample) {
    
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

WaterSensorSample* MainTask::getSensorWithLowestBattery() {
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

WaterSensorSample* MainTask::getSensorWithLowestRssi() {
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

WaterSensorSample* MainTask::getSensorWithLongestSleep() {
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

uint8_t MainTask::getActiveWaterSensorCount() {
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

void MainTask::createHttpResponse(HttpPacket* packet) {


    assert(packet->getPayload() != NULL);
    assert(packet->getPayload()->size() < 2000);

    char* payload = new char[packet->getPayload()->size() +1];
    pvPortSetTypeName(payload, "PayldM1");
    memcpy(payload, packet->getPayload()->getStr(), packet->getPayload()->size());
    payload[packet->getPayload()->size() ] = NULL;

    char* savePtr;

    char* pch = strtok_r(payload, " \t", &savePtr);
    if (strcmp("GET", pch) != 0) {
        return;
    }
    pch = strtok_r(NULL, " \t", &savePtr);
    char* resource = pch;


    if (strcmp("/status/heap", resource) == 0) {
       createHttpHeapStatusResponse(packet);
    } else if (strcmp("/status", resource) == 0) {
       createHttpStatusResponse(packet);
    } else if (strcmp("/display", resource) == 0) {
       createHttpDisplayResponse(packet);
    } else if (strcmp("/reset", resource) == 0) {
       createHttpResetResponse(packet, resource);
    } else if (strncmp("/valve", resource, 6) == 0) {
       createHttpOverrideResponse(packet, resource);
    } else if (strncmp("/test", resource, 5) == 0) {
       createHttpTestModeResponse(packet);
    } else {
       createHttpRootUsageResponse(packet);
    }

    delete[] payload;
}

void MainTask::createHttpStatusResponse(HttpPacket* packet) {
    checkStackLevel();
    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();

    char* buffer = new char[1024];
    pvPortSetTypeName(buffer, "bufferM2");
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM1");
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<head><style>table, th, td {border: 1px solid black;}</style></head>");
    msg->appendS("<body>");
    msg->appendS("<h1>ARM module</h1><br/>");

    msg->appendS("Status Reply Message<br/><br/>");
    clock->prettyPrint(buffer);
    msg->appendS(buffer);
    msg->appendS("<br/><br/>Main stack level = ");
    sprintf(buffer, "%.2f%%", STACK_LEVEL);
    msg->appendS(buffer);
    msg->appendS("<br/>Process stack level = ");
    sprintf(buffer, "%.2f%%", PROCESS_STACK_LEVEL);
    msg->appendS(buffer);

//    msg->appendS("<br/><br/>Global remaining heap free size = ");
//    sprintf(buffer, "%i", getRemainingHeapSize());
//    msg->appendS(buffer);

    msg->appendS("<br/><br/>FreeRTOS heap free size = ");
    sprintf(buffer, "%i", xPortGetFreeHeapSize());
    msg->appendS(buffer);

    msg->appendS("<br/><br/>FreeRTOS minimum ever heap free size = ");
    sprintf(buffer, "%i", xPortGetMinimumEverFreeHeapSize());
    msg->appendS(buffer);

    msg->appendS("<br/><br/>MainTask stack high water mark = ");
    sprintf(buffer, "%u", (unsigned int)uxTaskGetStackHighWaterMark(NULL /*this->handle*/));
    msg->appendS(buffer);

    msg->appendS("<br/>");
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();



    msg->appendS("<br/><b>Task list:</b><br/><pre>");
    msg->appendS("Name          State   Prio  StckHighWtr #<br/>");
    msg->appendS("-----------------------------------------<br/>");
    vTaskList(buffer);
    msg->appendS(buffer);
    msg->appendS("</pre>");

//    msg.appendS("<b>Runtime stats</b><pre>");
//    xTaskGetRunTimeStats(buffer);
//    msg.appendS(buffer);
//    msg.appendS("</pre>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

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

    msg->appendS("<br/>Messages<br/>");
    msg->appendS("<table>");
    msg->appendS("<tr><th>Date</th><th>Message</th></tr>");
    MessageRecord* mr = messageList->getFirst();
    while(mr != NULL) {
        msg->appendS("<tr><td>");
        clock->prettyPrint(buffer, &mr->date, &mr->time);
        msg->appendS(buffer);
        msg->appendS("</td><td>");
        msg->appendS(mr->text);
        msg->appendS("</td></tr>");
        mr = messageList->getNext(mr);
    }
    msg->appendS("</table>");
    msg->appendS("<br/>");

    msg->appendS("</body>");
    msg->appendS("</html>");



    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    taskYIELD();

    delete msg;
    delete[] buffer;
}

void MainTask::createHttpHeapStatusResponse(HttpPacket* packet) {
    uint32_t heapSTRUCT_SIZE	= ( ( sizeof ( BlockLink_t ) + ( 8 - 1 ) ) & ~0x7 );
    assert(heapSTRUCT_SIZE >= 8 && heapSTRUCT_SIZE < 100);
    assert(packet > (HttpPacket*)0x20000000 && packet < (HttpPacket*)0x20030000);

#ifdef MODIFIED_HEAP_4
    assert(osAllocatedHeapStart > (BlockLink_t*)0x20000000 && osAllocatedHeapStart < (BlockLink_t*)0x20030000);
    BlockLink_t* heapPtr = osAllocatedHeapStart;
    BlockLink_t* heapEndPtr = osAllocatedHeapStart;

    while (heapEndPtr->pxNextBlock != NULL) {
        // Record the end of heap before we stream any more data
        heapEndPtr = heapEndPtr->pxNextBlock;
    }
    assert(heapEndPtr > (BlockLink_t*)0x20000000 && heapEndPtr < (BlockLink_t*)0x20030000);
#endif


    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();

    char* buffer = new char[1024];
    pvPortSetTypeName(buffer, "bufferM3");
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM2");
    
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<head><style>table, th, td {border: 1px solid grey;}</style></head>");
    msg->appendS("<body>");
    msg->appendS("<h1>ARM module</h1><br/>");

    msg->appendS("OS Heap Contents<br/><br/>");

    msg->appendS("<br/><br/>FreeRTOS heap free size = ");
    itoa(xPortGetFreeHeapSize(), buffer, 10);
    msg->appendS(buffer);

    msg->appendS("<br/><br/>FreeRTOS minimum ever heap free size = ");
    itoa(xPortGetMinimumEverFreeHeapSize(), buffer, 10);
    msg->appendS(buffer);

    msg->appendS("<br/>");

#ifndef MODIFIED_HEAP_4
    msg->appendS("<br/><br/>***** MODIFIED_HEAP_4 is not defined *****<br/>");
#endif

#ifdef MODIFIED_HEAP_4

    msg->appendS("<br/><br/>OS Heap (0x2000 0000 - 0x2003 0000)<br/>");
    msg->appendS("<table>"); 
    uint32_t totalMemory = 0;

    
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    /*uint32_t rowCounter = 0;*/
    do {
       
        if ((heapPtr->xBlockSize & ~0x80000000) > 0x30000 
            || !(isValidHeapAddress((uint8_t*)heapPtr->pxNextBlock) || heapPtr->pxNextBlock == 0) 
            || !(isValidHeapAddress((uint8_t*)heapPtr->pxPrevBlock) || heapPtr->pxPrevBlock == 0)
            /*|| strlen(heapPtr->typeName) <= 0 && rowCounter++ > 0*/ ) {
            this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
            msg->clear();
            taskYIELD();

            msg->appendS("Memory corruption ~address ");
            itoa((uint32_t)(((uint8_t*)heapPtr) + heapSTRUCT_SIZE), buffer, 16);
            msg->appendS(buffer);

            this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
            msg->clear();
            taskYIELD();
            break;
        }


        msg->appendS("<tr>");
        msg->appendS("<td>0x");
        itoa((uint32_t)(((uint8_t*)heapPtr) + heapSTRUCT_SIZE), buffer, 16);
        msg->appendS(buffer);
        msg->appendS("</td>");
        msg->appendS("<td>");
        msg->appendS(heapPtr->typeName);
        msg->appendS("</td>");
        msg->appendS("<td>");
        totalMemory += (heapPtr->xBlockSize & 0x00FFFFFF);
        itoa(heapPtr->xBlockSize & 0x00FFFFFF, buffer, 10);
        msg->appendS(buffer);
        msg->appendS(" bytes");
        msg->appendS("</td></tr>");

        this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
        msg->clear();
        taskYIELD();

        if (heapPtr == heapEndPtr) break;
        heapPtr = heapPtr->pxNextBlock;
        assert(heapPtr > (BlockLink_t*)0x20000000 && heapPtr < (BlockLink_t*)0x20030000);

    } while (heapPtr != NULL);
    msg->appendS("</table>");

    msg->appendS("<br/>Total Heap = ");
    itoa(totalMemory, buffer, 10);
    msg->appendS(buffer);
    msg->appendS(" bytes<br/>");

 #endif

    msg->appendS("<br/>");
    msg->appendS("</body>");
    msg->appendS("</html>");


    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();
    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;
    delete[] buffer;
}

extern uint8_t*heap_4_heapStart;

bool MainTask::isValidHeapAddress(uint8_t* address) {
    if (address < heap_4_heapStart) return false;
    return (address < (heap_4_heapStart + configTOTAL_HEAP_SIZE));
}

//uint32_t MainTask::getRemainingHeapSize() {
//    taskENTER_CRITICAL();
//
//    uint8_t** pages = NULL;
//    uint8_t* page;
//    uint32_t heapSize = 0;
//    while(true) {
//        uint8_t** newPages = (pages == NULL? (uint8_t**)malloc(sizeof(uint32_t)) : (uint8_t**)realloc(pages, sizeof(pages) + sizeof(uint32_t)));
//        if (newPages == NULL) break;
//        page = (uint8_t*)malloc(256);
//        if (page == NULL) break;
//        newPages[sizeof(pages)] = page;
//        pages = newPages;
//    } 
//    heapSize = sizeof(pages) * 256;
//    for (uint32_t i=0; i<sizeof(pages); i++) {
//        free(pages[i]);
//    }
//    free(pages);
//
//    taskEXIT_CRITICAL();
//    return heapSize;
//}

void MainTask::createHttpDisplayResponse(HttpPacket* packet) {

    assertValidHeapObject(packet, NULL);

    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();

    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM3");
    
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<head><style>table, th, td {border: 1px solid black;}</style></head>");
    msg->appendS("<body>\n");
   
    msg->appendS("<h1>ARM module</h1><br/>");


    msg->appendS("<script type='text/javascript'>\n");


    uint32_t lcdWidth = lcd->getWidth();
    uint32_t lcdHeight = lcd->getHeight();
    msg->appendS("  var margin=10;\n");
    msg->appendS("  var viewportWidth = document.body.clientWidth - 2*margin;\n");
    msg->appendS("  var lcdWidth = "); msg->appendI(static_cast<int>(lcdWidth)); msg->appendS(";\n");
    msg->appendS("  var lcdHeight = "); msg->appendI(static_cast<int>(lcdHeight)); msg->appendS(";\n");
    msg->appendS("  var aspectRatio = lcdWidth/lcdHeight;\n");
    msg->appendS("  var viewportHeight = viewportWidth / aspectRatio;\n");

    msg->appendS("  var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');\n");
    msg->appendS("  svg.setAttribute('x', margin);\n");
    msg->appendS("  svg.setAttribute('y', margin);\n");
    msg->appendS("  svg.setAttribute('width', viewportWidth);\n");
    msg->appendS("  svg.setAttribute('height', viewportHeight);\n");
    msg->appendS("  var xform = document.createElementNS('http://www.w3.org/2000/svg', 'g');\n");
    msg->appendS("  xform.setAttribute(\"transform\", \"translate(\" + margin + \",\" + margin + \")scale(\" + (viewportWidth-2*margin)/lcdWidth + \", \" + (viewportHeight-2*margin)/lcdHeight + \")\");\n");
    msg->appendS("  svg.appendChild(xform);\n");

    assertValidHeapObject(packet, NULL);
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    assertValidHeapObject(msg, "ZstrM3");

    msg->appendS("var data=[");
    uint32_t chunkCounter = 0;
    for (uint32_t column=0; column<lcdWidth; column++) {
        for (uint32_t row=0; row<lcdHeight; row++) {
            uint8_t color = lcd->getPixel(column, row);
            if (color > 0) {
                color = 0xF - color;
                msg->appendI(static_cast<int>(row));
                msg->appendS(",");
                msg->appendI(static_cast<int>(column));
                msg->appendS(",'");
                msg->appendI(color);
                msg->appendS("',");
                
                chunkCounter++;
                if (chunkCounter > 100) {
                    assertValidHeapObject(packet, NULL);
                    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
                    msg->clear();
                    assertValidHeapObject(msg, "ZstrM3");
                    taskYIELD();
                    chunkCounter = 0;
                }
            }
        }
    }
    msg->appendS("-1,-1,-1];\n");

    assertValidHeapObject(packet, NULL);
    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    assertValidHeapObject(msg, "ZstrM3");

    msg->appendS("  var border = document.createElementNS('http://www.w3.org/2000/svg', 'rect');\n");
    msg->appendS("  border.setAttribute('x', 0);\n");
    msg->appendS("  border.setAttribute('y', 0);\n");
    msg->appendS("  border.setAttribute('width', lcdWidth);\n");
    msg->appendS("  border.setAttribute('height', lcdHeight);\n");
    msg->appendS("  border.setAttribute('stroke', '#88C');\n");
    msg->appendS("  border.setAttribute('fill', 'none');\n");
    msg->appendS("  xform.appendChild(border);\n");

    msg->appendS("var i = 0;\n");
    msg->appendS("while(true) {\n");
    msg->appendS("  if (data[i] < 0) break;\n");
    msg->appendS("  var rect = document.createElementNS('http://www.w3.org/2000/svg', 'rect');\n");
    msg->appendS("  rect.setAttribute('y', data[i++]);\n");
    msg->appendS("  rect.setAttribute('x', data[i++]);\n");
    msg->appendS("  rect.setAttribute('width', 1);\n");
    msg->appendS("  rect.setAttribute('height', 1);\n");
    msg->appendS("  rect.setAttribute('stroke', 'none');\n");
    msg->appendS("  rect.setAttribute('fill', '#' + data[i] + data[i] + data[i++]);\n");
    msg->appendS("  xform.appendChild(rect);\n");
    msg->appendS("}\n");
     
    msg->appendS("document.body.appendChild(svg);\n");

    msg->appendS("</script>\n");


    msg->appendS("</body>");
    msg->appendS("</html>");


    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();
    assertValidHeapObject(packet, NULL);
    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);

    assertValidHeapObject(msg, "ZstrM3");
    delete msg;
}

void MainTask::resetState(bool fromIsr) {
    this->waterValve->silenceAlarm();
    if (!testMode) {
        this->waterValve->openValve();
        this->waterValve->closeBypassValve();
    }
    this->setIsWet(false);
    this->setBlueSwitchLedState(false, fromIsr);
}

void MainTask::createHttpResetResponse(HttpPacket* packet, char* resource) {

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

void MainTask::createHttpTestModeResponse(HttpPacket* packet) {

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

void MainTask::createHttpOverrideResponse(HttpPacket* packet, char* resource) {
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

void MainTask::createHttpRootUsageResponse(HttpPacket* packet) {
    this->wiFiTransmitterTask->startChunkedIpv4Response(packet);
    taskYIELD();
    Zstring* msg = new Zstring();
    pvPortSetTypeName(msg, "ZstrM6");
    
    msg->appendS("<!doctype html>");
    msg->appendS("<html>");
    msg->appendS("<body>");
    msg->appendS("<h2>Usage:</h2>");
    msg->appendS("<ul>");

    msg->appendS("<li>/display - show current LCD screen</li>");
    msg->appendS("<li>/status - debugging information</li>");
    msg->appendS("<li>/status/heap - heap debugging information</li>");
    msg->appendS("<li>/test - start test mode for 1 hour</li>");
    msg->appendS("<li>/reset - restore dry operation</li>");
    msg->appendS("<li>/valve/[open|close]</li>");

    msg->appendS("</ul>");
    msg->appendS("</body></html>");

    this->wiFiTransmitterTask->sendIpv4ResponseChunk(packet, msg);
    msg->clear();
    taskYIELD();

    this->wiFiTransmitterTask->endChunkedIpv4Response(packet);
    delete msg;

}




