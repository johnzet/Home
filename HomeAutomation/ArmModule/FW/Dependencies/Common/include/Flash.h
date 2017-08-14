#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <string.h>

#define MEMORY_WRITTEN_MARKER ((uint32_t)(0x12345678))


uint32_t flashSector; //FLASH_SECTOR  FLASH_Sector_23
uint32_t baseFlashAddress; // BASE_ADDRESS_SECTOR_23 0x081E0000

class Flash {

    public:
        Flash(uint32_t sector, uint32_t baseAddress, uint32_t sectorLength);
        ~Flash();
        void init();
        void write(uint32_t addressOffset, byte[] data, uint32_t length);
        uint32_t getBaseAddress();
        uint8_t getErasedValue();


     private:
        void initPeripheral();
        void eraseSector();
        void initSector();
};

 