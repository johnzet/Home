#include <LCD.h>
#include <VideoBuffer.h>

VideoBuffer::VideoBuffer(uint32_t columns, uint32_t rows, uint8_t bitsPerPixel) {
    this->bufferMutex = xSemaphoreCreateMutex();
    this->columns = columns;
    this->rows = rows;
    this->bitsPerPixel = bitsPerPixel;
    this->pixelsPerByte = 8/bitsPerPixel;
    this->pages = rows / (this->pixelsPerByte);
    this->vBuffer = new uint8_t[columns * pages];
    pvPortSetTypeName(this->vBuffer, "VBuffer");
}
VideoBuffer::~VideoBuffer() {
    delete[] this->vBuffer;
}

void VideoBuffer::clear() {
    memset(this->vBuffer, NULL, this->columns * this->pages);
}

void VideoBuffer::setPixel(uint32_t column, uint32_t row, uint8_t color) {
    if (xSemaphoreTake(bufferMutex, portMAX_DELAY) == pdTRUE) {
        if (column >= this->columns) column = this->columns-1;
        if (row >= this->rows) row = this->rows-1;
        uint32_t page = row / this->pixelsPerByte;
        uint8_t pixel = row % this->pixelsPerByte;

        uint8_t* cell = (this->vBuffer + this->columns * page + column);
    
        uint8_t mask = 0b0;
        for (uint8_t i=0; i<this->bitsPerPixel; i++) {
            mask |= 1 << i;
        }
        mask = mask << (this->bitsPerPixel * (pixel));

        *cell &= ~mask;
        *cell = *cell | color << (uint8_t)(this->bitsPerPixel * (pixel));

        xSemaphoreGive(bufferMutex);
    }
}

uint8_t VideoBuffer::getPixel(uint32_t column, uint32_t row) {
    if (xSemaphoreTake(bufferMutex, portMAX_DELAY) == pdTRUE) {
        if (column >= this->columns) column = this->columns-1;
        if (row >= this->rows) row = this->rows-1;
        uint32_t page = row / this->pixelsPerByte;
        uint8_t pixel = row % this->pixelsPerByte;

        uint8_t* cell = (this->vBuffer + this->columns * page + column);
    
        uint8_t mask = 0b0;
        for (uint8_t i=0; i<this->bitsPerPixel; i++) {
            mask |= 1 << i;
        }

        mask = mask << (this->bitsPerPixel * (pixel));

        uint8_t value = (*cell & mask) >> (this->bitsPerPixel * (pixel));

        xSemaphoreGive(bufferMutex);
        return value;
    }
    return 0;
}

void VideoBuffer::drawLine(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color) {
    uint32_t xLength = abs(x2-x1);
    uint32_t yLength = abs(y2-y1);
    int32_t xIncrement = (x1<x2)? 1:-1;
    int32_t yIncrement = (y1<y2)? 1:-1;

    if (xLength == 0) {
        for(uint32_t y=y1; (yIncrement>0? y<=y2:y>=y2); y+= yIncrement) setPixel(x1, y, color); 
    } else  if (yLength == 0) {
        for(uint32_t x=x1; (xIncrement>0? x<=x2:x>=x2); x+= xIncrement) setPixel(x, y1, color); 
    } else if (xLength >= yLength) {
        for (uint32_t x=x1; (xIncrement>0? x<=x2:x>=x2); x+= xIncrement) {
            uint32_t y = y1 + yIncrement * (int)((float)yLength*((float)(x-x1)/(float)xLength));
            setPixel(x, y, color);
        }
    } else {
        for (uint32_t y=y1; (yIncrement>0? y<=y2:y>=y2); y+= yIncrement) {
            uint32_t x = x1 + xIncrement * (int)((float)xLength*((float)(y-y1)/(float)yLength));
            setPixel(x, y, color);
        }
    }
}

void VideoBuffer::drawRectangle(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color, bool fill) {
    drawLine(x1,y1, x2,y1, color);
    drawLine(x1,y1, x1,y2, color);
    drawLine(x2,y2, x2,y1, color);
    drawLine(x2,y2, x1,y2, color);

    if (fill) {
        int32_t yIncrement = (y1<y2)? 1:-1;
        for (uint32_t y=y1; (yIncrement>0? y<y2:y>y2); y+= yIncrement) {
            drawLine(x1,y, x2,y,color);
        }
    }
}

void VideoBuffer::drawChar(uint32_t x, uint32_t y, uint8_t charCode, Font* font, uint8_t color) {
    uint8_t* fontData = font->getFontData(charCode);
    uint32_t width = font->getCharWidth(charCode);
    uint32_t height = font->getHeight();
    uint8_t byteHeight = height/8;
    if ((height % 8) > 0) byteHeight++;

    for (uint8_t byteRow=0; byteRow<byteHeight; byteRow++) {
        for (uint8_t col=0; col<width; col++) {
            for (uint8_t bitIndex=0; bitIndex<8 && (byteRow*((uint32_t)0x08)+bitIndex)<height; bitIndex++) {
                uint8_t c = *(fontData + byteRow*width + col);
                uint8_t lastRowShift = ( (byteRow == byteHeight-1)? byteHeight*8-height : 0);
                if (c & (1 << (bitIndex + lastRowShift))) { 
                    setPixel(x+col, y+byteRow*8+bitIndex, color);
                }
            }
        }
    }
}

void VideoBuffer::drawString(uint32_t x, uint32_t y, char* str, Font* font, uint8_t color, uint32_t spacing) {
    uint32_t xPos = x;
    for (uint32_t i=0; i<strlen(str); i++) {
        if (i > 0) xPos += spacing;
        drawChar(xPos, y, str[i], font, color);
        xPos += font->getCharWidth(str[i]);
    }
}

uint8_t* VideoBuffer::getBuffer() {
    return this->vBuffer;
}

uint32_t VideoBuffer::getBufferLength() {
    return this->columns * this->pages;
}

uint32_t VideoBuffer::getWidth() {
    return this->columns;
}

uint32_t VideoBuffer::getHeight() {
    return this->rows;
}

#pragma GCC diagnostic ignored "-Wunused-parameter"
extern "C" void backlightTimerCallback (TimerHandle_t pxTimer) {
    GPIO_SetBits(BACKLIGHT_ENABLE_PORT, BACKLIGHT_ENABLE_PIN);
}

LCD::LCD() {
    this->videoBuffer = new VideoBuffer(240, 128, 4);
    pvPortSetTypeName(this->videoBuffer, "LcdVBfr");

    this->backlightTimerHandle = xTimerCreate(
        "Backlight Timer", 
        20000 / portTICK_PERIOD_MS, 
        pdFALSE /* repeat */,
        this /*  timer ID  */,
        backlightTimerCallback
   );

}

LCD::~LCD() {
    delete this->videoBuffer;
}

void LCD::init() {
    initIO();
    initLcd();
    initDma();

    videoBuffer->clear();
    
    FontGeorgia30x20 font = FontGeorgia30x20();
    drawString(10,10, "Booting", &font, 0b1111, 1);
    refreshLcd();


}

void LCD::initIO() {
    GPIO_InitTypeDef GPIO_InitStructure;

    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOC, ENABLE);

    GPIO_InitStructure.GPIO_Pin = BACKLIGHT_ENABLE_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_25MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOC, &GPIO_InitStructure);

    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_GPIOA, ENABLE);
    RCC_APB2PeriphClockCmd(RCC_APB2Periph_SPI1, ENABLE);

    GPIO_InitStructure.GPIO_Pin = RESET_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_25MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = CS_PIN | CD_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_OUT;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_25MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_InitStructure.GPIO_Pin = SCK_PIN | MOSI_PIN;
    GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF;
    GPIO_InitStructure.GPIO_Speed = GPIO_Speed_25MHz;
    GPIO_InitStructure.GPIO_OType = GPIO_OType_PP;
    GPIO_InitStructure.GPIO_PuPd = GPIO_PuPd_NOPULL;
    GPIO_Init(GPIOA, &GPIO_InitStructure);

    GPIO_PinAFConfig(GPIOA, SCK_PIN_SOURCE, GPIO_AF_SPI1);
    //GPIO_PinAFConfig(GPIOA, CS_PIN_SOURCE, GPIO_AF_SPI1);
    GPIO_PinAFConfig(GPIOA, MOSI_PIN_SOURCE, GPIO_AF_SPI1);

    SPI_InitTypeDef SPI_InitStruct;
 	SPI_InitStruct.SPI_Direction = SPI_Direction_1Line_Tx;
	SPI_InitStruct.SPI_Mode = SPI_Mode_Master;
	SPI_InitStruct.SPI_DataSize = SPI_DataSize_8b;
	SPI_InitStruct.SPI_CPOL = SPI_CPOL_High;
	SPI_InitStruct.SPI_CPHA = SPI_CPHA_2Edge;
	SPI_InitStruct.SPI_NSS = SPI_NSS_Soft;
	SPI_InitStruct.SPI_BaudRatePrescaler = SPI_BaudRatePrescaler_16;
	SPI_InitStruct.SPI_FirstBit = SPI_FirstBit_MSB;
	SPI_Init(SPI1, &SPI_InitStruct); 
	
	SPI_Cmd(SPI1, ENABLE); // enable SPI1

}

void LCD::initLcd() {
    GPIO_SetBits(CS_PORT, CS_PIN);
    GPIO_ResetBits(RESET_PORT, RESET_PIN);
    GPIO_SetBits(BACKLIGHT_ENABLE_PORT, BACKLIGHT_ENABLE_PIN);

    delay_ms(10);

    uint8_t contrast = 0x9C;
    uint8_t view = 0x02;

    GPIO_SetBits(RESET_PORT, RESET_PIN);
    delay_ms(200);
    GPIO_ResetBits(CS_PORT, CS_PIN);

	sendCommand(0xF1); //last COM electrode
	sendCommand(0x7F); 
	sendCommand(0xF2); //Display start line
	sendCommand(0x00); 
	sendCommand(0xF3); //Display end line
	sendCommand(0x7F); 
	
    sendCommand(0x81);
	sendCommand(contrast);

    sendCommand(0xC0);
	sendCommand(view);
	
	sendCommand(0xA3); // LC line rate
    sendCommand(0x25); // TC
	sendCommand(0xAB); // DC 4 bits per pixel
	sendCommand(0xD1); // DC display pattern - 4 bits per pixel


    GPIO_SetBits(CS_PORT, CS_PIN);

}

void LCD::initDma() {
    RCC_AHB1PeriphClockCmd(RCC_AHB1Periph_DMA2, ENABLE);

    DMA_InitTypeDef  DMA_InitStructure;
    DMA_DeInit(DMA2_Stream5);

    DMA_InitStructure.DMA_Channel = DMA_Channel_3;
    DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t)&SPI1->DR;
    DMA_InitStructure.DMA_Memory0BaseAddr = (uint32_t)this->videoBuffer->getBuffer();;
    DMA_InitStructure.DMA_DIR = DMA_DIR_MemoryToPeripheral;
    DMA_InitStructure.DMA_BufferSize = this->videoBuffer->getBufferLength();
    DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
    DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
    DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
    DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
    DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
    DMA_InitStructure.DMA_Priority = DMA_Priority_Medium;
    DMA_InitStructure.DMA_FIFOMode = DMA_FIFOMode_Disable;
    DMA_InitStructure.DMA_FIFOThreshold = 0;
    DMA_InitStructure.DMA_MemoryBurst = DMA_MemoryBurst_Single;
    DMA_InitStructure.DMA_PeripheralBurst = DMA_PeripheralBurst_Single;  
    
    DMA_Init(DMA2_Stream5, &DMA_InitStructure);
    DMA_ITConfig(DMA2_Stream5, DMA_IT_TC, ENABLE);
 

    NVIC_InitTypeDef NVIC_InitStructure;
    NVIC_InitStructure.NVIC_IRQChannel = DMA2_Stream5_IRQn;
    NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelSubPriority = 0;
    NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;
    NVIC_Init(&NVIC_InitStructure);
 
    DMA_Cmd(DMA2_Stream5, ENABLE);

    
}

extern "C" void DMA2_Stream5_IRQHandler(void) {
    if(DMA_GetITStatus(DMA2_Stream5, DMA_IT_TCIF5)) {
        GPIO_SetBits(CS_PORT, CS_PIN);

        //Clear DMA1 Channel1 Half Transfer, Transfer Complete and Global interrupt pending bits
         DMA_ClearITPendingBit(DMA2_Stream5, DMA_IT_TCIF5);
         DMA_ClearITPendingBit(DMA2_Stream5, DMA_IT_HTIF5);
         DMA_ClearITPendingBit(DMA2_Stream5, DMA_IT_FEIF5);
         DMA_ClearITPendingBit(DMA2_Stream5, DMA_IT_DMEIF5);
         DMA_ClearITPendingBit(DMA2_Stream5, DMA_IT_TEIF5);
    }
}

void LCD::resetScreenPointers() {
    sendCommand(0x00);
    sendCommand(0x10);
    sendCommand(0x60);
    sendCommand(0x70);
}

uint8_t LCD::getPixel(uint32_t column, uint32_t row) {
    return this->videoBuffer->getPixel(column, row);
}

void LCD::refreshLcd() {
    GPIO_ResetBits(CS_PORT, CS_PIN);

    resetScreenPointers();

    SPI_I2S_DMACmd(SPI1, SPI_I2S_DMAReq_Tx, ENABLE); 
    DMA_Cmd ( DMA2_Stream5, ENABLE);
}

void LCD::getFontMetrics(Font* font, char* str, uint32_t* width, uint32_t* height, uint32_t spacing) {
    *height = font->getHeight();
    *width = 0;
    for(uint32_t i=0; i<strlen(str); i++) {
        if (i>0) *width += spacing;
       *width += font->getCharWidth(str[i]);
    }
}

void LCD::drawTestScreen() {
    VideoBuffer *vb = this->videoBuffer;

    vb->drawLine(0,0, 239, 127, 0b1011);
    vb->drawLine(0, 127, 239, 0, 0b1101);
    vb->drawLine(0, 0, 239, 0, 0b1110);
    vb->drawLine(0, 0, 0, 127, 0b1111);
    vb->drawLine(239, 0, 239, 127, 0b1011);
    vb->drawLine(0, 127, 239, 127, 0b1101);

    vb->drawRectangle(20,20, 220,107, 0b0100, true);
    vb->drawRectangle(20,20, 220,107, 0b1111, false);

    uint32_t x = 30;
    uint32_t y = 25;
    uint32_t width=0;
    uint32_t height=0;
    uint32_t spacing = 1;

    Font* font = new FontGeorgia16x10(); 
    pvPortSetTypeName(font, "Ggia1610");
    char* str = "Georgia 16x10";
    getFontMetrics(font, str, &width, &height, spacing);
    vb->drawRectangle(x-1,y-1, x+width+1, y+height+1, 0b0000, true);
    vb->drawString(x, y, str, font, 0b1111, spacing);
    vb->drawRectangle(x-2,y-2, x+width+2, y+height+2, 0b0111, false);

    x = 30;
    y = 45;
    width=0;
    height=0;
    str = "Georgia 30x20";
    font = new FontGeorgia30x20();
    pvPortSetTypeName(font, "Ggia3020");
    getFontMetrics(font, str, &width, &height, spacing);
    vb->drawRectangle(x-1,y-1, x+width+1, y+height+1, 0b0000, true);
    vb->drawString(x, y, str, font, 0b1111, spacing);
    vb->drawRectangle(x-2,y-2, x+width+2, y+height+2, 0b0111, false);

    x = 30;
    y = 72;
    width=0;
    height=0;
    str = "Georgia 10x7";
    font = new FontGeorgia10x7();
    pvPortSetTypeName(font, "Ggia10x7");
    getFontMetrics(font, str, &width, &height, spacing);
    vb->drawRectangle(x-1,y-1, x+width+1, y+height+1, 0b0000, true);
    vb->drawString(x, y, str, font, 0b1111, spacing);
    vb->drawRectangle(x-2,y-2, x+width+2, y+height+2, 0b0111, false);

    x = 30;
    y = 90;
    width=0;
    height=0;
    str = "Georgia 7x5";
    font = new FontGeorgia7x5();
    pvPortSetTypeName(font, "Ggia7x5");
    getFontMetrics(font, str, &width, &height, spacing);
    vb->drawRectangle(x-1,y-1, x+width+1, y+height+1, 0b0000, true);
    vb->drawString(x, y, str, font, 0b1111, spacing);
    vb->drawRectangle(x-2,y-2, x+width+2, y+height+2, 0b0111, false);

    refreshLcd();
}

void LCD::_sendByte(uint8_t b) {
    waitSpiDone();
  	SPI1->DR = b;
  	waitSpiDone();
 }
  
void LCD::sendCommand(uint8_t cmd) {
    GPIO_ResetBits(CD_PORT, CD_PIN);
    _sendByte(cmd);
    GPIO_SetBits(CD_PORT, CD_PIN);
}

void LCD::waitSpiDone() {
	while( !(SPI1->SR & SPI_I2S_FLAG_TXE) ); // wait until transmit complete
	while( SPI1->SR & SPI_I2S_FLAG_BSY ); // wait until SPI is not busy anymore
}

void LCD::drawLine(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color) {
    this->videoBuffer->drawLine(x1, y1, x2, y2, color);
}

void LCD::drawRectangle(uint32_t x1, uint32_t y1, uint32_t x2, uint32_t y2, uint8_t color, bool fill) {
    this->videoBuffer->drawRectangle(x1, y1, x2, y2, color, fill);
}

void LCD::drawString(uint32_t x, uint32_t y, char* str, Font* font, uint8_t color, uint32_t spacing) {
    this->videoBuffer->drawString(x, y, str, font, color, spacing);
}

void LCD::clear() {
    this->videoBuffer->clear();
}

uint32_t LCD::getWidth() {
    return this->videoBuffer->getWidth();
}

uint32_t LCD::getHeight() {
    return this->videoBuffer->getHeight();
}

