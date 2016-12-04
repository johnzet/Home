#include <HttpPacket.h>

HttpPacket::HttpPacket() { 
    this->payload = NULL;
    frameType = 0;
    frameId = 0;
    address = 0;
    destPort = 0;
    sourcePort = 0;
    protocol = 0;
    options = 0;
}

HttpPacket::HttpPacket(HttpPacket* p) {
    this->payload = NULL;
    this->setAddress(p->getAddress());
    this->setDestPort(p->getDestPort());
    this->setFrameId(p->getFrameId());
    this->setFrameType(p->getFrameType());
    this->setOptions(p->getOptions());
    this->setPayload(p->getPayload());
    this->setProtcol(p->getProtcol());
    this->setSourcePort(p->getSourcePort());
}

HttpPacket::~HttpPacket() {
    if (this->payload != NULL) {
        delete this->payload;
        this->payload = NULL;
    }
}

void HttpPacket::setFrameType(uint8_t frameType) {
     this->frameType = frameType;
}

void HttpPacket::setFrameId(uint8_t frameId) {
    this->frameId = frameId;
}

void HttpPacket::setAddress(uint32_t address) {
    this->address = address;
}

void HttpPacket::setDestPort(uint16_t destPort) {
    this->destPort = destPort;
}

void HttpPacket::setSourcePort(uint16_t sourcePort) {
    this->sourcePort = sourcePort;
}

void HttpPacket::setProtcol(uint8_t protocol) {
    this->protocol = protocol;
}

void HttpPacket::setOptions(uint8_t options) {
    this->options = options;
}

void HttpPacket::setPayload(Zstring* p) {
    if (this->payload != NULL) {
        delete this->payload;
    }
    this->payload = new Zstring(p->getStr(), p->size());
    pvPortSetTypeName(this->payload, "HttpPkPy");
}




uint8_t HttpPacket::getFrameType() {
     return this->frameType;
}

uint8_t HttpPacket::getFrameId() {
    return this->frameId;
}

uint32_t HttpPacket::getAddress() {
    return this->address;
}

uint16_t HttpPacket::getDestPort() {
    return this->destPort;
}

uint16_t HttpPacket::getSourcePort() {
    return this->sourcePort;
}

uint8_t HttpPacket::getProtcol() {
    return this->protocol;
}

uint8_t HttpPacket::getOptions() {
    return this->options;
}

Zstring* HttpPacket::getPayload() {
    return this->payload;
}

