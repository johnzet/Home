#include <AtCmdPacket.h>

AtCmdPacket::AtCmdPacket() { 
    this->payload = NULL;
    atCmd[0] = ' ';
    atCmd[1] = ' ';
    status = NULL;
}

AtCmdPacket::~AtCmdPacket() {
    if (this->payload != NULL) {
        delete this->payload;
        this->payload = NULL;
    }
}

void AtCmdPacket::setAtCmd(char* cmd) {
    memcpy(this->atCmd, cmd, 2);
}

void AtCmdPacket::setStatus(uint8_t status) {
    this->status = status;
}

void AtCmdPacket::setPayload(Zstring* payload) {
    this->payload = new Zstring(payload->getStr(), payload->size());
    pvPortSetTypeName(this->payload, "AtCmdPy");
}

char* AtCmdPacket::getAtCmd() {
    return this->atCmd;
}

uint8_t AtCmdPacket::getStatus() {
    return this->status;
}

Zstring* AtCmdPacket::getPayload() {
    if (this->payload == NULL) {
        this->payload = new Zstring();
        pvPortSetTypeName(this->payload, "AtCdPy2");
    }
    return this->payload;
}


void RemoteAtCmdPacket::setAddress(uint32_t address) {
    this->address = address;
}

uint32_t RemoteAtCmdPacket::getAddress() {
    return this->address;
}



