#include <XBeeTask.h>
#define DELIMITERS " "

extern "C" {
    XBeeTask* irqXbeeObject;
}

XBeeTask::XBeeTask(QueueHandle_t sampleQ) {
    this->sampleQueue = sampleQ;
    irqXbeeObject = this;
    atCmdRxPacket = NULL;
}

XBeeTask::~XBeeTask() {
}

void XBeeTask::init() {

    usartQueue = xQueueCreate(200, sizeof(uint8_t));
}

void XBeeTask::task() {
    initXBeeUsart();  // start interrupts after the scheduler starts

    while(true) {
        if (this->heartbeatSemaphore != NULL) {
            xSemaphoreGive(this->heartbeatSemaphore);
        }

        char c;
        if (xQueueReceive(this->usartQueue, &c, 10/portTICK_PERIOD_MS) == pdPASS) {
            loadPacketByte(c);
        }

//        if (httpRequestReceived) {
//            leds.setGreenState(true);  leds.setRedState(false);
//            httpRequestReceived = false;
//            handleHttpRequest(this->packet);
//           leds.setGreenState(false);  leds.setRedState(false);
//        }
    }
}



void XBeeTask::initXBeeUsart() {
        GPIO_InitTypeDef GPIO_InitStructure;
        USART_InitTypeDef USART_InitStructure;

        /* enable peripheral clock for USART2 */
        RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE);

        /* GPIOA clock enable */
        RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);

        /* Connect USART2 pins to AF7 */
        // TX = PA2, RX = PA3
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource2, GPIO_AF_USART2);
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource3, GPIO_AF_USART2);

        //  RX & TX
        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_2 | GPIO_Pin_3;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        // CTS (input)
        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_0;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        // RTS (output)
        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_1;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        USART_InitStructure.USART_BaudRate = 230400;
        USART_InitStructure.USART_WordLength = USART_WordLength_8b;
        USART_InitStructure.USART_StopBits = USART_StopBits_1;
        USART_InitStructure.USART_Parity = USART_Parity_No;
        USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_RTS_CTS;
        USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
        USART_Init(USART2, &USART_InitStructure);
    
        USART_ITConfig(USART2, USART_IT_RXNE, ENABLE);

        NVIC_InitTypeDef NVIC_InitStructure;
        NVIC_InitStructure.NVIC_IRQChannel = USART2_IRQn;
        NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = configLOWEST_PRIORITY_INTERRUPT;
        NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
        NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
        NVIC_Init(&NVIC_InitStructure);        

        USART_Cmd(USART2, ENABLE); // enable USART2
}



// this is the interrupt request handler (IRQ) for ALL USART2 interrupts
extern "C" void USART2_IRQHandler(void){
    //checkStackLevel();
    if (irqXbeeObject != NULL) {
        irqXbeeObject->irqHandler();
    }
}

void XBeeTask::irqHandler() {
    
    if( USART_GetITStatus(USART2, USART_IT_RXNE) != RESET) {
        uint8_t c = USART2->DR;
        BaseType_t xHigherPriorityTaskWoken;
        xQueueSendFromISR(this->usartQueue, &c, &xHigherPriorityTaskWoken);
        if (xHigherPriorityTaskWoken != pdFALSE) {
            taskYIELD();
        }
    }
}


void XBeeTask::loadPacketByte(uint8_t c) {
    static uint16_t count = 0;
    static uint16_t packetLength;  // includes the frame type byte up to the checksum (not incl)
    static uint8_t frameType = 0;
    bool isFirstByte;
    bool isLastByte;
    bool isCkSumByte = false; 
    static uint8_t cksum = 0;
    static bool escapedChar = false;
    static char buffer[100];

    if (c == 0x7D) {
        escapedChar = true;
        return;
    }

    if (escapedChar) {
        c = unEscapeChar(c);
        escapedChar = false;
    }

    if (count == 0) { 
        frameType = 0;
        packetLength = 0;
    }
    
    isLastByte = packetLength > 0 && (count >= (packetLength+2));
    isCkSumByte = packetLength > 0 && (count >= (packetLength+3));
    isFirstByte = (count == 4);

    if (!isCkSumByte) {
        switch(frameType) {
            case(0):
                // normal until the frame type is parsed
                break;
            case(0x92):
                loadDataSamplePacketByte(c, isFirstByte, isLastByte);
                break;
            case(0x95):
                loadNodeIdPacketByte(c, isFirstByte, isLastByte);
                break;
            case(0x88):
                loadAtCmdResponsePacketByte(c, isFirstByte, isLastByte);
                break;
            case(0x97):
                loadRemoteAtCmdResponsePacketByte(c, isFirstByte, isLastByte);
                break;
            default:
                leds->setRedState(true);
                sprintf(buffer, "XBee: Unknown frame ID: 0x%X", frameType);
                messageList->addMessage(buffer);
                count = 0;
        }
    }

    switch(count) {
        case(0):
            if (c == 0x7e) {
                packetLength = 0;
                leds->setRedState(false);
                count++;
            } else {
                leds->setRedState(true);
                sprintf(buffer, "XBee: Waiting for 0x7E, got 0x%X", c);
                messageList->addMessage(buffer);
                count = 0;
            }
            cksum = 0;
            break;
        case(1):
            packetLength = ((uint8_t)c) << 8;
            count++;
            break;
        case(2):
            packetLength += (uint8_t)c;
            count++;
            if (packetLength >= 200) {
                leds->setRedState(true);
                sprintf(buffer, "XBee: Packet length too long: %i", packetLength);
                messageList->addMessage(buffer);
                count = 0;
            }
            break;
        case(3):
            frameType = (uint8_t)c;
            if (frameType == 0x92|| frameType == 0x95 || frameType == 0x88 || frameType == 0x97) {
                cksum += frameType;
                count++;
            } else if (frameType == 0xFE) {
                leds->setRedState(true);
                sprintf(buffer, "XBee: Frame Error");
                messageList->addMessage(buffer);
                count = 0;
            } else {
                leds->setRedState(true);
                sprintf(buffer, "XBee: Unknown frame ID: 0x%X", frameType);
                messageList->addMessage(buffer);
                count = 0;
            }
            break;
        default:
            if (isCkSumByte) {
                uint8_t cksumByte = c;
                if (cksumByte != (0xff-cksum)) {
//                   if (frameType != 0xB0) {
                    leds->setRedState(true);
//                }
                sprintf(buffer, "XBee: Bad checksum");
                messageList->addMessage(buffer);
                }
                count = 0;
            }  else {   
                cksum += c;
                count++;
            }
            break;
    }
}

void XBeeTask::loadDataSamplePacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint16_t count = 0;
    static uint64_t sourceAddress = 0;
    static uint16_t senderAddress = 0;
    static uint16_t digitalSample = 0;
    static uint16_t analogSample = 0;

    if (isFirstByte) {
        count = 0;
    }
    
    uint64_t wc = c;

    switch(count++) {
        case(0):
            sourceAddress = wc << 56;
            break;
        case(1):
            sourceAddress += wc << 48;
            break;
        case(2):
            sourceAddress += wc << 40;
            break;
        case(3):
            sourceAddress += wc << 32;
            break;
        case(4):
            sourceAddress += wc << 24;
            break;
        case(5):
            sourceAddress += wc << 16;
            break;
        case(6):
            sourceAddress += wc << 8;
            break;
        case(7):
            sourceAddress += wc;
            break;
        case(8):
            senderAddress = wc << 8;
            break;
        case(9):
            senderAddress += wc;
            break;
        case(10):
            // 0x01 = packet acknowledged, 0x02 == packet was a broadcast
            break;
        case(11):
            // Number of samples - always 1
            break;
        case(12):
            // digital bitmask MSB
            break;
        case(13):
            // digital bitmask LSB
            break;
        case(14):
            // analog bitmask
            break;
        case(15):
            // digital samples MSB
            digitalSample = c << 8;
            break;
        case(16):
            // digital samples LSB
            digitalSample += c;
            break;
        case(17):
            // first analog sample MSB
            analogSample = c << 8;
            break;
        case(18):
            // first analog sample LSB
            analogSample += c;
            break;
        default:
            break;
    }
    if (isLastByte) {
            WaterSensorSample sample = WaterSensorSample();
            sample.setAddress(sourceAddress);
            sample.setIsWet(!(digitalSample & 0b10));
            sample.setBatteryVoltage(((float)analogSample) / 1000.0);
            sample.setDate(clock->getDate());
            sample.setTime(clock->getTime());
            xQueueSend(this->sampleQueue, &sample, 1000/portTICK_PERIOD_MS);
            count = 0;
    }        
}

void XBeeTask::loadNodeIdPacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint16_t count = 0;
    static uint64_t sourceAddress = 0;
    static uint64_t remoteAddress = 0;
    static uint16_t senderAddress = 0;
    static bool niParsed = false;
    static Zstring* niString = new Zstring();
    pvPortSetTypeName(niString, "ZstrX1");
    static bool isEndDevice = false;

    if (isFirstByte) {
        count = 0;
        niParsed = false;
        niString->clear();
    }

    uint64_t wc = c;

    switch(count++) {
        case(0):
            sourceAddress = wc << 56;
            break;
        case(1):
            sourceAddress += wc << 48;
            break;
        case(2):
            sourceAddress += wc << 40;
            break;
        case(3):
            sourceAddress += wc << 32;
            break;
        case(4):
            sourceAddress += wc << 24;
            break;
        case(5):
            sourceAddress += wc << 16;
            break;
        case(6):
            sourceAddress += wc << 8;
            break;
        case(7):
            sourceAddress += wc;
            break;
        case(8):
            senderAddress = wc << 8;
            break;
        case(9):
            senderAddress += wc;
            break;
        case(10):
            // 0x01 = packet acknowledged, 0x02 == packet was a broadcast
            break;
        case(11):
            // 16-bit remote address MSB
            break;
        case(12):
            // 16-bit remote address LSB
            break;
        case(13):
            remoteAddress = wc << 56;
            break;
        case(14):
            remoteAddress += wc << 48;
            break;
        case(15):
            remoteAddress += wc << 40;
            break;
        case(16):
            remoteAddress += wc << 32;
            break;
        case(17):
            remoteAddress += wc << 24;
            break;
        case(18):
            remoteAddress += wc << 16;
            break;
        case(19):
            remoteAddress += wc << 8;
            break;
        case(20):
            remoteAddress += wc;
            break;
        default:
            if (!niParsed) {
                if (c == NULL) {
                    niParsed = true;
                } else {
                    niString->append8(c);
                }
            } else {
                if (count == 20 + niString->size() + 5) {
                    if (c == 2) {
                        isEndDevice = true;
                    }
                }
            }
    }
    if (isLastByte) {
        if (isEndDevice) {
            WaterSensorSample sample = WaterSensorSample();
            sample.setAddress(sourceAddress);
            sample.setName(niString->getStr(), niString->size());
            sample.setDate(clock->getDate());
            sample.setTime(clock->getTime());
            xQueueSend(this->sampleQueue, &sample, 100/portTICK_PERIOD_MS);
        }
    }
}

void XBeeTask::loadAtCmdResponsePacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint8_t count = 0;
    static char atCmd[] = {0,0};
    static uint8_t status = 0;
    static Zstring response = Zstring();

    if (isFirstByte) {
        count = 0;
        response.clear();
    }


    switch(count++) {
        case(0):
            break;
        case(1):
            atCmd[0] = c;;
            break;
        case(2):
            atCmd[1] = c;
            break;
        case(3):
            status = c;
            break;
        default:
            response.append8(c);
            break;
    }

    if (isLastByte) {
        if (this->atCmdRxPacket != NULL) {
            delete this->atCmdRxPacket;
            this->atCmdRxPacket = NULL;
        }
        AtCmdPacket* p = new AtCmdPacket();
        pvPortSetTypeName(p, "AtCmdX1");

        p->setAtCmd(atCmd);
        p->setStatus(status);
        p->setPayload(&response);
        response.clear();

        this->atCmdRxPacket = p;
        atCmdResponseReceived = true;
        count = 0;
    }
}

void XBeeTask::loadRemoteAtCmdResponsePacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint8_t count = 0;
    static char atCmd[] = {0,0};
    static uint64_t address;
    static Zstring response = Zstring();

    if (isFirstByte) {
        count = 0;
        response.clear();
    }

    uint64_t wc = c;

    switch(count++) {
        case(0):
            break;
        case(1):
            address = wc << 56;
            break;
        case(2):
            address += wc << 48;
            break;
        case(3):
            address += wc << 40;
            break;
        case(4):
            address += wc << 32;
            break;
        case(5):
            address += wc << 24;
            break;
        case(6):
            address += wc << 16;
            break;
        case(7):
            address += wc << 8;
            break;
        case(8):
            address += wc;
            break;
        case(9):
//            address16 = wc << 8;
            break;
        case(10):
//            address16 += wc;
            break;
        case(11):
            atCmd[0] = wc;
            break;
        case(12):
            atCmd[1] = c;
            break;
        case(13):
            break;
        default:
            response.append8(c);
            break;
    }

    if (isLastByte) {
        WaterSensorSample sample = WaterSensorSample();
        sample.setAddress(address);
        if (atCmd[0] == 'N' && atCmd[1] == 'I') {
            sample.setName(response.getStr(), response.size());
        }
        if (atCmd[0] == 'D' && atCmd[1] == 'B') {
            sample.setRssi(response.getStr()[0]);
        }
        
        sample.setDate(clock->getDate());
        sample.setTime(clock->getTime());
        xQueueSend(this->sampleQueue, &sample, 1000/portTICK_PERIOD_MS);

        response.clear();
        count = 0;
    }
}

void XBeeTask::xbeeWriteBuffer(Zstring* buffer) {
    uint8_t cksum = 0;
    for (uint32_t i=0; i<buffer->size(); i++) {
        while (USART_GetFlagStatus(USART2, USART_FLAG_TXE) == RESET);
        char c = buffer->getChar(i);
        if (i > 2) {
            cksum += (uint8_t)c;
            if (c == 0x7E || c == 0x7D || c == 0x11 || c == 0x13) {
                USART_SendData(USART2, 0x7D);
                c = c ^ 0x20;
                while (USART_GetFlagStatus(USART2, USART_FLAG_TXE) == RESET);
            }
        }
        USART_SendData(USART2, c);
    }

    while (USART_GetFlagStatus(USART2, USART_FLAG_TXE) == RESET);
    USART_SendData(USART2, (uint8_t)(0xff - cksum));
}

void XBeeTask::sendAtCmd(AtCmdPacket* packet) {
    Zstring apiPacket = Zstring();

    apiPacket.append8((uint8_t)0x7e);
    uint16_t length = packet->getPayload()->size() + 4;
    apiPacket.append16((uint16_t)length);
    apiPacket.append8(0x08);
    apiPacket.append8(0x01);
    apiPacket.appendS(packet->getAtCmd(), 2);
    apiPacket.appendS(packet->getPayload()->getStr());

    xbeeWriteBuffer(&apiPacket);
}

void XBeeTask::sendRemoteAtCmd(RemoteAtCmdPacket* packet) {
    Zstring apiPacket = Zstring();

    apiPacket.append8((uint8_t)0x7e);
    uint16_t length = packet->getPayload()->size() + 15;
    apiPacket.append16((uint16_t)length);
    apiPacket.append8(0x17);
    apiPacket.append8(0x01);
    apiPacket.append32(0x0013A200);  // XBee common address
    apiPacket.append32(packet->getAddress());
    apiPacket.append16(0xFFFE);
    apiPacket.append8(0x00);
    apiPacket.appendS(packet->getAtCmd(), 2);
    apiPacket.appendS(packet->getPayload()->getStr());

    xbeeWriteBuffer(&apiPacket);
}

bool XBeeTask::getAtCmdResponsePacket(AtCmdPacket* packet) {
    if (atCmdResponseReceived) {
        atCmdResponseReceived = false;
        packet->setStatus(this->atCmdRxPacket->getStatus());
        packet->setPayload(this->atCmdRxPacket->getPayload());
        return true;
    }
    return false;
}

void XBeeTask::requestName(WaterSensorSample* sample) {
    RemoteAtCmdPacket packet = RemoteAtCmdPacket();
    packet.setAtCmd("NI");
    packet.setAddress(sample->getAddress());
    this->sendRemoteAtCmd(&packet);
}

void XBeeTask::requestRssi(WaterSensorSample* sample) {
    RemoteAtCmdPacket packet = RemoteAtCmdPacket();
    packet.setAtCmd("DB");
    packet.setAddress(sample->getAddress());
    this->sendRemoteAtCmd(&packet);
}


//  Escape characters
//  When sending or receiving a UART data frame, specific data values must be escaped (flagged) so they do not interfere
//  with the data frame sequencing. To escape an interfering data byte, insert 0x7D and follow it with the byte to be
//  escaped XOR’d with 0x20.
//  Data bytes that need to be escaped:
//  • 0x7E – Frame Delimiter
//  • 0x7D – Escape
//  • 0x11 – XON
//  • 0x13 – XOFF
//
//  The packet length is calculated on the raw (un-escaped) bytes.
//  Likewise, the checksum is calculated on the raw (un-escaped) bytes.

bool XBeeTask::escapeChar(char* c) {
    if (*c == 0x7E || *c == 0x7D || *c == 0x11 || *c == 0x13) {
        *c = *c ^ 0x20;
        return true;
    }
    return false;
}

char XBeeTask::unEscapeChar(char c) {
    return c ^ 0x20;
}

