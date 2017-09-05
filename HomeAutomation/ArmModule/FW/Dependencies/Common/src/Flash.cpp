#include <Flash.h>

void Flash::init(MessageList* messageList) {
        flashSector = FLASH_Sector_11;
        baseFlashAddress = 0x080E0000;
        flashBankSize = 1 << 17;  // 128k

        


}

//bool Flash::loadConfigFromFlash() {
//    uint32_t marker;
//    uint32_t version;
//#ifndef TARGET_IS_SIMULATOR
//    memcpy(&marker, (uint8_t*)baseFlashAddress, 4);
//    memcpy(&version, (uint8_t*)baseFlashAddress+4, 4);
//#else
//    marker = 0x00000000;
//    version = 0xFFFFFFFF;
//#endif
//    if (marker != MEMORY_WRITTEN_MARKER) return false;
//    if (version != CONFIG_VERSION) return false;
//    memcpy(this, (uint8_t*)(baseFlashAddress + 8), sizeof(Config));  // 8 bytes for MEMORY_WRITTEN_MARKER and CONFIG_VERSION
//    consoleEnabled = false;
//    return true;
//}
//
//void Flash::saveConfigToFlash() {
//    FLASH_Unlock();  
//    FLASH_ClearFlag(FLASH_FLAG_EOP | FLASH_FLAG_OPERR |FLASH_FLAG_WRPERR | FLASH_FLAG_PGAERR |FLASH_FLAG_PGPERR | FLASH_FLAG_PGSERR);
//    FLASH_Status flashStatus = FLASH_EraseSector(flashSector, VOLTAGE_RANGE);
//    if (flashStatus != FLASH_COMPLETE) {
//        printf("erase flash status = %i" NEWLINE, flashStatus);
//        return;
//    }
//
//    FLASH_ProgramByte(baseFlashAddress, (uint8_t)(MEMORY_WRITTEN_MARKER & 0xFF));
//    FLASH_ProgramByte(baseFlashAddress+1, (uint8_t)((MEMORY_WRITTEN_MARKER & 0xFF00) >> 8));
//    FLASH_ProgramByte(baseFlashAddress+2, (uint8_t)((MEMORY_WRITTEN_MARKER  & 0xFF0000) >> 16));
//    FLASH_ProgramByte(baseFlashAddress+3, (uint8_t)((MEMORY_WRITTEN_MARKER  & 0xFF000000) >> 24));
//    FLASH_ProgramByte(baseFlashAddress+4, (uint8_t)(CONFIG_VERSION & 0xFF));
//    FLASH_ProgramByte(baseFlashAddress+5, (uint8_t)((CONFIG_VERSION & 0xFF00) >> 8));
//    FLASH_ProgramByte(baseFlashAddress+6, (uint8_t)((CONFIG_VERSION  & 0xFF0000) >> 16));
//    FLASH_ProgramByte(baseFlashAddress+7, (uint8_t)((CONFIG_VERSION  & 0xFF000000) >> 24));
//
//    uint8_t* data = (uint8_t*)this;
//    uint32_t size = sizeof(Config);
//    for (uint8_t index=0; index<size; index++) {
//        uint32_t address = baseFlashAddress + 8 + index;  // 8 bytes for MEMORY_WRITTEN_MARKER and CONFIG_VERSION
//        flashStatus = FLASH_ProgramByte(address, *(data+index));
//        if (flashStatus != FLASH_COMPLETE) {
//            printf("write flash status = %i" NEWLINE, flashStatus);
//            return;
//        }
//    }
//    FLASH_Lock();
//return true;
//}

uint32_t Flash::getFlashMemoryBankStart() {
    return baseFlashAddress;
}

uint32_t Flash::getFlashMemoryBankSize() {
    return flashBankSize;
}
