#ifndef HTTP_PACKET_H
#define HTTP_PACKET_H
#include <stm32f4xx.h>
#include <__cross_studio_io.h>
#include <arm_const_structs.h>
#include <Zstring.h>

class HttpPacket {

// Only TX packets have frameIds
// Options byte is the status for RX packets

    private:
        uint8_t frameType;
        uint8_t frameId;
        uint8_t protocol;
        uint8_t options;
        uint32_t address;
        uint16_t destPort;
        uint16_t sourcePort;
        Zstring* payload;

    public:
        HttpPacket();
        explicit HttpPacket(HttpPacket* httpPacket);
        ~HttpPacket();

        void setFrameType(uint8_t frameType);
        void setFrameId(uint8_t frameId);
        void setAddress(uint32_t address);
        void setDestPort(uint16_t destPort);
        void setSourcePort(uint16_t sourcePort);
        void setProtcol(uint8_t protocol);
        void setOptions(uint8_t options);
        void setPayload(Zstring* payload);

        uint8_t getFrameType();
        uint8_t getFrameId();
        uint32_t getAddress();
        uint16_t getDestPort();
        uint16_t getSourcePort();
        uint8_t getProtcol();
        uint8_t getOptions();
        Zstring* getPayload();

};

#endif
