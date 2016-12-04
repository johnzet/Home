#ifndef VideoBuffer_H
#define VideoBuffer_H

#pragma GCC diagnostic ignored "-Wwrite-strings"

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <stm32f4xx_spi.h>
#include <stm32f4xx_rcc.h>
#include <stm32f4xx_dma.h>
#include <delay.h>
#include <stdlib.h>
#include <string.h>
#include <FreeRTOS.h>
#include <semphr.h>


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


class VideoBuffer {
    private:
        uint32_t columns;
        uint32_t rows;
        uint32_t pages;
        uint8_t bitsPerPixel;
        uint8_t pixelsPerByte;
        uint8_t *vBuffer;
        SemaphoreHandle_t bufferMutex;


    public:
        VideoBuffer(uint32_t columns, uint32_t rows, uint8_t bitsPerPixel);
        ~VideoBuffer();
        void clear();
        void setPixel(uint32_t column, uint32_t row, uint8_t color);
        uint8_t getPixel(uint32_t column, uint32_t row);
        void drawLine(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color); 
        void drawRectangle(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color, bool fill); 
        void drawChar(uint32_t x, uint32_t y, uint8_t charCode, Font* font, uint8_t color);
        void drawString(uint32_t x, uint32_t y, char* str, Font* font, uint8_t color, uint32_t spacing);
        uint32_t getWidth();
        uint32_t getHeight();

        uint8_t* getBuffer();
        uint32_t getBufferLength();

};

#endif