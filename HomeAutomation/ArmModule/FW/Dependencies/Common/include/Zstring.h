#ifndef ZSTRING_H
#define ZSTRING_H

#include <stm32f4xx.h>
#include <stm32f4xx_conf.h>
#include <FreeRTOS.h>
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <assert.h>


extern "C" bool isHeapAddress(void* address);

class Zstring {

private: 
    char* str;
    uint32_t length;
    char buffer[30];

public:
    Zstring();
    explicit Zstring(char* buffer);
    explicit Zstring(char* buffer, uint32_t length);
//    Zstring(Zstring& s);
//    Zstring(Zstring* s);
    ~Zstring();
    void appendS(char* buffer, uint32_t length);
    void appendS(char* buffer);
    void append8(uint8_t value);
    void append16(uint16_t bigEndianValue);
    void append32(uint32_t bigEndianValue);
    void appendI(int intValue);
    void appendI(int intValue, int base);
    void appendZ(Zstring* zstring);
    char getChar(uint32_t index);
    char* getStr();
    uint32_t size();
    void clear();
};

#endif
