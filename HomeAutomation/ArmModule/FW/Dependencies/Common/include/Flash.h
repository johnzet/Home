#include <stm32f4xx.h>
#include <__cross_studio_io.h>
#include <string.h>
#include <MessageList.h>
 
#define MEMORY_WRITTEN_MARKER ((uint32_t)(0x12345678))
 
class Flash {
 
    private:
        uint32_t flashSector;
        uint32_t baseFlashAddress;
        uint32_t flashBankSize;
 
    public:
        void init(MessageList* messageList);
        uint32_t getFlashMemoryBankStart();
        uint32_t getFlashMemoryBankSize();
        void erase();
};
 