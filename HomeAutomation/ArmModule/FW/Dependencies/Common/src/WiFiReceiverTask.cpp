#include <WiFiReceiverTask.h>
#define DELIMITERS " "

extern "C" {
    WiFiReceiverTask* irqWiFiObject;
}

WiFiReceiverTask::WiFiReceiverTask(QueueHandle_t httpRequestQueue, QueueHandle_t transmissionStatusQueue) {
    httpRequestReceived = false;
    httpResponseReceived = false;
    modemHasJoined = false;
    atCmdRxPacket = NULL;
    workingBuffer[0] = NULL;
    irqWiFiObject = this;
    modemStatus = 0;
    this->httpRequestQueue = httpRequestQueue;
    this->transmissionStatusQueue = transmissionStatusQueue;

    packet = NULL;
    responsePacket = NULL;
}

WiFiReceiverTask::~WiFiReceiverTask() {
    if (this->atCmdRxPacket != NULL) {
        delete this->atCmdRxPacket;
        this->atCmdRxPacket = NULL;
    }
    if (this->packet != NULL) {
        delete this->packet;
        this->packet = NULL;
    }
    if (this->responsePacket != NULL) {
        delete this->responsePacket;
        this->responsePacket = NULL;
    }
}

void WiFiReceiverTask::init() {
    usartQueue = xQueueCreate(1000, sizeof(uint8_t));
    pvPortSetTypeName(usartQueue, "usartQ_W");
}

void WiFiReceiverTask::task() {
    initWiFiUsart();  // start interrupts after the scheduler starts

    while(true) {
        if (this->heartbeatSemaphore != NULL) {
            xSemaphoreGive(this->heartbeatSemaphore);
        }

        char c;
        if (xQueueReceive(this->usartQueue, &c, 10/portTICK_PERIOD_MS) == pdPASS) {
            loadPacketByte(c);
        }

        if (httpRequestReceived) {
            leds->setGreenState(true);  leds->setRedState(false);
            httpRequestReceived = false;
            handleHttpRequest(this->packet);
            this->packet = NULL;
            leds->setGreenState(false);  leds->setRedState(false);
        }
    }
}



void WiFiReceiverTask::initWiFiUsart() {
        GPIO_InitTypeDef GPIO_InitStructure;
        USART_InitTypeDef USART_InitStructure;

        /* enable peripheral clock for USART1 */
        RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

        /* GPIOA clock enable */
        RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);

        /* Connect USART1 pins to AF7 */
        // TX = PA9, RX = PA10
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource9,  GPIO_AF_USART1);
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource10, GPIO_AF_USART1);
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource11, GPIO_AF_USART1);
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource12, GPIO_AF_USART1);

        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9 | GPIO_Pin_10 | GPIO_Pin_12;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_UP;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        USART_InitStructure.USART_BaudRate = 230400;
        USART_InitStructure.USART_WordLength = USART_WordLength_8b;
        USART_InitStructure.USART_StopBits = USART_StopBits_1;
        USART_InitStructure.USART_Parity = USART_Parity_No;
        USART_InitStructure.USART_HardwareFlowControl = USART_HardwareFlowControl_RTS_CTS;
        USART_InitStructure.USART_Mode = USART_Mode_Rx | USART_Mode_Tx;
        USART_Init(USART1, &USART_InitStructure);
    
        USART_ITConfig(USART1, USART_IT_RXNE, ENABLE);

        NVIC_InitTypeDef NVIC_InitStructure;
        NVIC_InitStructure.NVIC_IRQChannel = USART1_IRQn;
        NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = configLOWEST_PRIORITY_INTERRUPT;
        NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
        NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
        NVIC_Init(&NVIC_InitStructure);        

        USART_Cmd(USART1, ENABLE); // enable USART1
}



// this is the interrupt request handler (IRQ) for ALL USART1 interrupts
extern "C" void USART1_IRQHandler(void){
    if (irqWiFiObject != NULL) {
        irqWiFiObject->irqHandler();
    }
}

void WiFiReceiverTask::irqHandler() {
    
    if( USART_GetITStatus(USART1, USART_IT_RXNE) != RESET) {
        uint8_t c = USART1->DR;
        BaseType_t xHigherPriorityTaskWoken;
        xQueueSendFromISR(this->usartQueue, &c, &xHigherPriorityTaskWoken);
        if (xHigherPriorityTaskWoken != pdFALSE) {
            taskYIELD();
        }
    }
}


// API frame names                          API ID
// Tx64 Request                             0x00
// Remote Command Request                   0x07
// AT Command                               0x08
// AT Command - Queue Parameter Value       0x09
// ZigBee Transmit Packet                   0x10
// ZigBee Explicit Transmit Packet          0x11
// ZigBee Remote AT Command                 0x17
// TX IPv4                                  0x20
// Send Data Request                        0x28
// Device Response                          0x2A
// Rx64 Indicator                           0x80
// Remote Command Response                  0x87
// AT Command Response                      0x88
// TX Status                                0x89
// Modem Status                             0x8A
// ZigBee TX Status                         0x8B
// IO Data Sample Rx Indicator              0x8F
// ZigBee Receive Packet                    0x90
// Explicit ZigBee Receive Packet           0x91
// ZigBee Remote AT Command Response        0x97
// RX IPv4                                  0xB0
// Send Data Response                       0xB8
// Device Request                           0xB9
// Device Response Status                   0xBA
// Frame Error                              0xFE

void WiFiReceiverTask::loadPacketByte(uint8_t c) {

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
            case(0xB0):
                loadIpV4PacketByte(c, isFirstByte, isLastByte);
                break;
            case(0x88):
                loadAtCmdResponsePacketByte(c, isFirstByte, isLastByte);
                break;
            case(0x89):
                loadTransmissionStatusPacketByte(c, isFirstByte, isLastByte);
                break;
            case(0x8A):
                loadModemStatusPacketByte(c, isFirstByte, isLastByte);
                break;
            default:
                leds->setRedState(true);
                sprintf(buffer, "WiFi: Unknown frame ID: 0x%X", frameType);
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
                //sprintf(buffer, "WiFi: Waiting for 0x7E, got 0x%X", c);  // noisy
                //messageList->addMessage(buffer);
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
            if (packetLength >= BUFFER_LENGTH) {
                leds->setRedState(true);
                sprintf(buffer, "WiFi: Packet length too long: %i", packetLength);
                messageList->addMessage(buffer);
                count = 0;
            }
            break;
        case(3):
            frameType = (uint8_t)c;
            if (frameType == 0xB0|| frameType == 0x88 || frameType == 0x89 || frameType == 0x8A) {
                cksum += frameType;
                count++;
            } else if (frameType == 0xFE) {
                leds->setRedState(true);
                bool success = false;
                xQueueSend(this->transmissionStatusQueue, &success, 100/portTICK_PERIOD_MS);

                // sprintf(buffer, "WiFi: Frame Error\r\n");   // noisy
                //messageList->addMessage(buffer);
                count = 0;
            } else {
                leds->setRedState(true);
                sprintf(buffer, "WiFi: Unknown frame ID: 0x%X", frameType);
                messageList->addMessage(buffer);
                count = 0;
            }
            break;
        default:
            if (isCkSumByte) {
                uint8_t cksumByte = c;
                if (cksumByte != (0xff-cksum)) {
                   if (frameType != 0xB0) {
                    leds->setRedState(true);
                }
                    sprintf(buffer, "WiFi: Bad checksum" NEWLINE);
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


void WiFiReceiverTask::loadIpV4PacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint16_t count = 0;
    static uint8_t frameType = 0;
    static uint32_t address = 0;
    static uint16_t destPort = 0;
    static uint16_t sourcePort = 0;
    static uint8_t protocol = 0;
    static uint8_t options = 0;

    if (isFirstByte) {
        count = 0;
    }

    switch(count++) {
        case(0):
            address = c << 24;
            break;
        case(1):
            address += c << 16;
            break;
        case(2):
            address += c << 8;
            break;
        case(3):
            address += c;
            break;
        case(4):
            destPort = c << 8;
            break;
        case(5):
            destPort += c;
            break;
        case(6):
            sourcePort = c << 8;
            break;
        case(7):
            sourcePort += c;
            break;
        case(8):
            protocol = c;
            break;
        case(9):
            options = c;
            break;
        default:
            assert(count-10 < BUFFER_LENGTH && count-11 >= 0);
            workingBuffer[count-11] = c;
            workingBuffer[count-10] = NULL;
            if (isLastByte) {
                Zstring payload = Zstring();
                payload.appendS(this->workingBuffer, (uint32_t)(count-11));

                HttpPacket* p = new HttpPacket();
                pvPortSetTypeName(p, "HttpPkW1");
                p->setFrameType(frameType);
                p->setFrameId(0);
                p->setAddress(address);
                p->setDestPort(destPort);
                p->setSourcePort(sourcePort);
                p->setProtcol(protocol);
                p->setOptions(options);
                p->setPayload(&payload);

                if (this->packet != NULL) {
                    delete this->packet;
                    this->packet = NULL;
                }
                this->packet = p;

                assert(payload.size() < 2000);
                char* resource = new char[payload.size()];
                pvPortSetTypeName(resource, "charArW1");
                bool isRequest = (sscanf(payload.getStr(), "GET %50s HTTP", resource) > 0);
                char version;
                bool isResponse = (sscanf(payload.getStr(), "HTTP/1.%c 200 OK", &version) > 0);
                if (isRequest) {
                    httpRequestReceived = true;
                } else if (isResponse) {
                    httpResponseReceived = true;
                    if (this->responsePacket != NULL) {
                        delete this->responsePacket;
                        this->responsePacket = NULL;
                    }
                    this->responsePacket = new HttpPacket(this->packet);
                    pvPortSetTypeName(this->responsePacket, "HttpPkW2");
                }
                delete[] resource;
                count = 0;
            }
            break;
    }
}

void WiFiReceiverTask::loadAtCmdResponsePacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

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
            atCmd[0] = c;
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
        pvPortSetTypeName(p, "AtCmdW1");
        p->setAtCmd(atCmd);
        p->setStatus(status);
        p->setPayload(&response);
        response.clear();

        this->atCmdRxPacket = p;
        count = 0;
        
        bool success = (status == 0);
        xQueueSend(this->transmissionStatusQueue, &success, 100/portTICK_PERIOD_MS);

    }
}

void WiFiReceiverTask::loadModemStatusPacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint8_t count = 0;
    static uint8_t status = 0;

    if (isFirstByte) {
        count = 0;
    }

    switch(count++) {
        case(0):
            status = c;
            this->modemHasJoined = (status == 0x02);
            break;
        default:
            break;
    }

    if (isLastByte) {
//        this->transmissionStatus = status;
        bool success = (status == 2);
        xQueueSend(this->transmissionStatusQueue, &success, 100/portTICK_PERIOD_MS);

        count = 0;
    }
}

void WiFiReceiverTask::loadTransmissionStatusPacketByte(uint8_t c, bool isFirstByte, bool isLastByte) {

    static uint8_t count = 0;
    static uint8_t status = 0;

    if (isFirstByte) {
        count = 0;
    }

    switch(count++) {
        case(1):
            status = c;
            break;
        default:
            break;
    }

    if (isLastByte) {
        this->modemStatus = status;
        bool success = (status == 0);
        xQueueSend(this->transmissionStatusQueue, &success, 100/portTICK_PERIOD_MS);
        count = 0;
    }
}

void WiFiReceiverTask::handleHttpRequest(HttpPacket* p) {
    xQueueSend(this->httpRequestQueue, &p, 100/portTICK_PERIOD_MS);
}

bool WiFiReceiverTask::getAtCmdResponsePacket(AtCmdPacket* packet) {
    if (this->atCmdRxPacket == NULL) {
        return false;
    }
    
    packet->setAtCmd(this->atCmdRxPacket->getAtCmd());
    packet->setStatus(this->atCmdRxPacket->getStatus());
    packet->setPayload(this->atCmdRxPacket->getPayload());

    return true;
}

HttpPacket* WiFiReceiverTask::getIpv4TxResponsePacket() {
    if (httpResponseReceived) {
        httpResponseReceived = false;
        return this->responsePacket;
    }
    return NULL;
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

bool WiFiReceiverTask::escapeChar(char* c) {
    if (*c == 0x7E || *c == 0x7D || *c == 0x11 || *c == 0x13) {
        *c = *c ^ 0x20;
        return true;
    }
    return false;
}

char WiFiReceiverTask::unEscapeChar(char c) {
    return c ^ 0x20;
}












