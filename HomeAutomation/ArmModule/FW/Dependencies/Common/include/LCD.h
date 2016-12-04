#ifndef LCD_H
#define LCD_H

#pragma GCC diagnostic ignored "-Wwrite-strings"

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <stm32f4xx_spi.h>
#include <stm32f4xx_rcc.h>
#include <stm32f4xx_dma.h>
#include <Fonts/FontGeorgia30x20.h>
#include <Fonts/FontGeorgia16x10.h>
#include <Fonts/FontGeorgia10x7.h>
#include <Fonts/FontGeorgia7x5.h>
#include <delay.h>
#include <stdlib.h>
#include <string.h>
#include <FreeRTOS.h>
#include <semphr.h>
#include <timers.h>
#include <VideoBuffer.h>


#define BACKLIGHT_ENABLE_PORT GPIOC
#define BACKLIGHT_ENABLE_PIN GPIO_Pin_9
#define SCK_PORT GPIOA
#define SCK_PIN GPIO_Pin_5
#define SCK_PIN_SOURCE GPIO_PinSource5
#define CS_PORT GPIOA
#define CS_PIN GPIO_Pin_4
#define CS_PIN_SOURCE GPIO_PinSource4
#define MOSI_PORT GPIOA
#define MOSI_PIN GPIO_Pin_7
#define MOSI_PIN_SOURCE GPIO_PinSource7
#define CD_PORT GPIOA
#define CD_PIN GPIO_Pin_6
#define CD_PIN_SOURCE GPIO_PinSource6
#define RESET_PORT GPIOA
#define RESET_PIN GPIO_Pin_8
#define RESET_PIN_SOURCE GPIO_PinSource8



class LCD {

    private:
        VideoBuffer* videoBuffer;
        void _sendByte(uint8_t b);
        void sendCommand(uint8_t b);
        void sendData(uint8_t b);
        void initIO();
        void initLcd();
        void initDma();
        void waitSpiDone();
        void resetScreenPointers();
        //void backlightTimerCallback(TimerHandle_t pxTimer);

    public:
        LCD();
        ~LCD();
        void init();
        uint32_t getWidth();
        uint32_t getHeight();
        void refreshLcd();
        uint8_t getPixel(uint32_t column, uint32_t row);

        void drawTestScreen();
        TimerHandle_t backlightTimerHandle;

        void clear();
        void getFontMetrics(Font* font, char* str, uint32_t* width, uint32_t* height, uint32_t spacing);
        void drawLine(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color); 
        void drawRectangle(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color, bool fill); 
        void drawString(uint32_t x, uint32_t y, char* str, Font* font, uint8_t color, uint32_t spacing);
        

        void displayErrorMsg(char* msg);
};

#endif