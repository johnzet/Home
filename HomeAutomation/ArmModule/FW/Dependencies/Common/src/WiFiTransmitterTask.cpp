#include <WiFiTransmitterTask.h>

WiFiTransmitterTask::WiFiTransmitterTask(MessageList* messageList, QueueHandle_t transmissionStatusQueue) {
    this->transmissionStatusQueue = transmissionStatusQueue;
    this->messageList = messageList;
}

WiFiTransmitterTask::~WiFiTransmitterTask() {
}

void WiFiTransmitterTask::init() {

}
 
void WiFiTransmitterTask::task() {
    while(true) {
        if (this->heartbeatSemaphore != NULL) {
            xSemaphoreGive(this->heartbeatSemaphore);
        }
        vTaskDelay(100);
    }
}

void WiFiTransmitterTask::sendAtCmd(AtCmdPacket* packet) {
    uint8_t count = 0;
    bool success = false;
    do {
        Zstring* apiPacket = new Zstring();
        pvPortSetTypeName(apiPacket, "ZstrW3");
    
        apiPacket->append8((uint8_t)0x7e);
        uint16_t length = packet->getPayload()->size() + 4;
        apiPacket->append16(length);
        apiPacket->append8(0x08);
        apiPacket->append8(0x01);
        apiPacket->appendS(packet->getAtCmd(), 2);
        apiPacket->appendS(packet->getPayload()->getStr());

        success = wifiWriteBuffer(apiPacket);
        delete apiPacket;
        count++;
    } while (!success && count < 10);
}

void WiFiTransmitterTask::addIpv4TxRequestHeaderToPayload(HttpPacket* packet, char* resource) {
    char* buffer = new char[20];

    Zstring payload = Zstring("GET ");
    payload.appendS(resource);
    payload.appendS(" HTTP/1.0\r\n");
    payload.appendS("Host: ");
    formatIpAddress(buffer, packet->getAddress());
    payload.appendS(buffer);
    payload.appendS("\r\n");
    payload.appendS("Content-type: */*; charset=UTF-8\r\n");
    //payload.appendS("Transfer-Encoding: chunked\r\n");

    payload.appendS("Connection: close\r\n");
    payload.appendS("Content-Length: ");
    payload.appendI(0);
    payload.appendS("\r\n");
    payload.appendS("\r\n");  // indicates the end of headers

    packet->setPayload(&payload);
    delete[] buffer;
}

void WiFiTransmitterTask::sendIpv4TxRequestPacket(HttpPacket* packet, char* resource) {
    uint8_t count = 0;
    bool success = false;
    do {
        addIpv4TxRequestHeaderToPayload(packet, resource);
        Zstring* apiPacket = assembleIpV4TxPacket(packet);
        pvPortSetTypeName(apiPacket, "ZstrW2");
        success = wifiWriteBuffer(apiPacket);
        delete apiPacket;
        count++;
    } while (!success && count < 10);
}

bool WiFiTransmitterTask::wifiWriteBuffer(Zstring* buffer) {
    xQueueReset(this->transmissionStatusQueue);
    uint8_t cksum = 0;
    for (uint32_t i=0; i<buffer->size(); i++) {
        while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET);
        char c = buffer->getChar(i);
        if (i > 2) {
            cksum = (cksum + c) & 0xFF;
        }
        if (i > 0) {
            if (c == 0x7E || c == 0x7D || c == 0x11 || c == 0x13) {
                USART_SendData(USART1, 0x7D);
                c = c ^ 0x20;
                while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET);
            }
        }
        USART_SendData(USART1, c);
    }

    while (USART_GetFlagStatus(USART1, USART_FLAG_TXE) == RESET);
    USART_SendData(USART1, (0xff - cksum) & 0xFF);

    bool returnValue;
    if ((xQueueReceive(this->transmissionStatusQueue, &returnValue, 100/portTICK_PERIOD_MS) != pdPASS) || returnValue == false) {
        //messageList->addMessage("WiFiTransmission failure");
        return false;
    }
    return true;
}


void WiFiTransmitterTask::startChunkedIpv4Response(HttpPacket* packet) {
    uint8_t count = 0;
    bool success = false;
    do {
        char *buffer = new char[20];
        pvPortSetTypeName(buffer, "charArW3");
        Zstring payload = Zstring("HTTP/1.1 200 OK\r\n");
    //    payload.appendS("Date: Wed, 15 Jun 2016 01:28:30 GMT\r\n");
        payload.appendS("Host: ");
        formatIpAddress(buffer, packet->getAddress());
        payload.appendS(buffer);
        payload.appendS("\r\n");
        payload.appendS("Content-type: text/html; charset=UTF-8\r\n");
        payload.appendS("Transfer-Encoding: chunked\r\n");

        payload.appendS("Connection: close\r\n");
        payload.appendS("\r\n");
        packet->setPayload(&payload);

        Zstring* apiPacket = assembleIpV4TxPacket(packet);
        success = wifiWriteBuffer(apiPacket);
        delete apiPacket;
        delete[] buffer;
        count++;
    } while (!success && count < 10);
}

void WiFiTransmitterTask::sendIpv4ResponseChunk(HttpPacket* packet, Zstring* chunk) {
    uint8_t count = 0;
    bool success = false;
    do {
        Zstring *payload = new Zstring();
        pvPortSetTypeName(payload, "ZstrW4");
        payload->appendI(chunk->size(), 16);
        payload->appendS("\r\n");
        payload->appendZ(chunk);
        payload->appendS("\r\n");

        packet->setPayload(payload);
        delete payload;

        Zstring* apiPacket = assembleIpV4TxPacket(packet);
        success = wifiWriteBuffer(apiPacket);

        delete apiPacket;
        count++;
    } while (!success && count < 10);
}

void WiFiTransmitterTask::endChunkedIpv4Response(HttpPacket* packet) {
    Zstring end = Zstring();
    sendIpv4ResponseChunk(packet, &end);
}

Zstring* WiFiTransmitterTask::assembleIpV4TxPacket(HttpPacket* packet) {
    static uint8_t frameId = 0;
    frameId++;
    if (frameId == 0) {   // frameID = 0 disables transmission status packets
        frameId = 1;
    }

    Zstring* p = new Zstring();
    pvPortSetTypeName(p, "ZstrW1");

    p->append8((uint8_t)0x7e);
    assert(packet->getPayload()->size() < 0x30000);
    uint16_t length = packet->getPayload()->size() + 12;
    p->append16(length);

    p->append8(0x20);
    p->append8(frameId);
    uint32_t a = packet->getAddress();
    p->append32(a);

    uint16_t port = packet->getSourcePort();
    p->append16(port);

    p->append16(0x0000);

    p->append8(0x01);
    p->append8(0x00);

    p->appendZ(packet->getPayload());

    return p;
}

void WiFiTransmitterTask::formatIpAddress(char* buffer, uint32_t address) {
    sprintf(buffer, "%u.%u.%u.%u", (address & 0xff000000)>>24, (address & 0x00ff0000)>>16, (address & 0x0000ff00)>>8, (address & 0x000000ff));
}

