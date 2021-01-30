#include <WiFiTask.h>
#define DELIMITERS " "

extern "C" {
    WiFiTask* irqWiFiObject;
}

WiFiTask::WiFiTask() {
}

void WiFiTask::init() {
    httpRequestReceived = false;
    httpRequestBuffer[0] = NULL;
    irqWiFiObject = this;
}

void WiFiTask::task() {
    initWiFiUsart();  // start interrupts after the scheduler starts

    while(true) {
        if (httpRequestReceived) {
            httpRequestReceived = false;
            handleHttpRequest(httpRequestBuffer);
            httpRequestBuffer[0] =  NULL;
            staleBuffer[0] = NULL;
        }
        vTaskDelay(50/portTICK_PERIOD_MS);
    }
}

void WiFiTask::initWiFiUsart() {
        GPIO_InitTypeDef GPIO_InitStructure;
        USART_InitTypeDef USART_InitStructure;

        /* enable peripheral clock for USART1 */
        RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

        /* GPIOA clock enable */
        RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);

        /* Connect USART1 pins to AF7 */
        // TX = PA9, RX = PA10
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource9, GPIO_AF_USART1);
        GPIO_PinAFConfig(GPIOA, GPIO_PinSource10, GPIO_AF_USART1);

        //  RX & TX
        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_9 | GPIO_Pin_10;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        // CTS (input)
        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_11;
        GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
        GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
        GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
        GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
        GPIO_Init(GPIOA, &GPIO_InitStructure);

        // RTS (output)
        GPIO_InitStructure.GPIO_Pin = GPIO_Pin_12;
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

void WiFiTask::irqHandler() {
    
    if( USART_GetITStatus(USART1, USART_IT_RXNE) != RESET) {
        loadPacketByte((uint8_t)USART1->DR);
    }
}

void WiFiTask::loadPacketByte(uint8_t c) {

    static uint16_t count = 0;
    static bool escapeSeuence = false;

    static uint16_t packetLength;  // includes the frame type byte up to the checksum (not incl)
    static uint8_t frameType = 0;
    static uint8_t cksum = 0;

    switch(count) {
        case(0):
            if (c == 0x7e) {
                count++;
                packetLength = 0;
            } else {
                printf("Waiting for 0x&E, got %x" NEWLINE, c);
                count = 0;
            }
            break;
        case(1):
            packetLength = 256 * (uint8_t)c;
            count++;
            break;
        case(2):
            packetLength += (uint8_t)c;
            count++;
            if (packetLength >= BUFFER_LENGTH) {
                printf("Packet length too long: %f" NEWLINE, packetLength);
                count = 0;
            }
            break;
        case(3):
            frameType = (uint8_t)c;
            if (frameType == 0xB0) {
                cksum += frameType;
                count++;
            } else {
                printf("Unknown frame type: %x" NEWLINE, frameType);
                count = 0;
            }
            break;
        default:
            if (count == (packetLength+3)) {
                uint8_t cksumByte = c;
                if (cksumByte != 0xff-cksum) {
                    printf("Bad checksum" NEWLINE);
                    count = 0;
                } else {
                    httpRequestReceived = true;
                    count = 0;
                }
            }  else {   
                httpRequestBuffer[count-14] = c;
                httpRequestBuffer[count-13] = NULL;
                cksum += c;
            }
            count++;
            break;
    }


}


void WiFiTask::wifiReplyOk(char* msg) {

    char tmpBuffer[10];
    wifiWriteBuffer("HTTP/1.1 200 OK\n");
    wifiWriteBuffer("Content-Type: text/html\n");
    char* doctype = "<!doctype html><html><h1>ARM module</h1><br/>";
    int contentLength = strlen(doctype) + strlen(msg);
    wifiWriteBuffer("Content-Length: ");
    wifiWriteBuffer(itoa(contentLength, tmpBuffer, 10));
    wifiWriteBuffer(doctype);
    wifiWriteBuffer(msg);
}

void WiFiTask::wifiWriteBuffer(char *b) {
    for (int i=0; i<strlen(b); i++) {
        while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET);
        USART_SendData(USART1, b[i]);
    }
}

void WiFiTask::handleHttpRequest(char* requestBuffer) {
    char* pch = strtok(requestBuffer, " \t");
    if (strcmp("GET", pch) != 0) {
        printf("Unknown request method: %s" NEWLINE, pch);
        return;
    }
    pch = strtok(NULL, " \t");
    char* resource = pch;
    
}

