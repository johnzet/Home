#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <string.h>

#pragma GCC diagnostic ignored "-Wunused-parameter"

//extern FlagStatus watchDogResetFlag;

#define MEMORY_WRITTEN_MARKER ((uint32_t)(0x12345678))
#define CONFIG_VERSION ((uint32_t)(0x00000001))


uint32_t flashSector; //FLASH_SECTOR  FLASH_Sector_23
uint32_t baseFlashAddress; // BASE_ADDRESS_SECTOR_23 0x081E0000

class Flash {

    public:
        bool loadConfigFromFlash();
        void setUseTestFlashBank(bool useTestBank);

};

 