#ifndef AT_CMD_RX_PACKET_H
#define AT_CMD_RX_PACKET_H

#include <stm32f4xx.h>
#include <__cross_studio_io.h>
#include <arm_const_structs.h>
#include <Zstring.h>

class AtCmdPacket {

    private:
        char atCmd[2];
        uint8_t status;
        Zstring* payload;

    public:
        AtCmdPacket();
        ~AtCmdPacket();

        void setAtCmd(char* atCmd);
        void setStatus(uint8_t status);
        void setPayload(Zstring* response);

        char* getAtCmd();
        uint8_t getStatus();
        Zstring* getPayload();

};

class RemoteAtCmdPacket : public AtCmdPacket {

    private:
        uint32_t address;

    public:
        void setAddress(uint32_t address);
        uint32_t getAddress();
};

#endif