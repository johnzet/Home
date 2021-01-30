
#ifndef Lcd_H
#define Lcd_H

#include <Arduino.h>
#include <inttypes.h>

#define SCK 10
#define MOSI 11
#define MISO 12
#define SS 9


class LcdClass
{
  private:
	bool displayIsOn;
	char lcdText[80];
	char lcdBuffer[80];
	void sendWord(uint16_t data, uint8_t length);
    void clearDisplay(void);
    void writeString(char *text, uint8_t lineNumber);
	void _writeLcd(const char *message);

  public:
	LcdClass(void);
	void displayOn(void);
	void displayOff(void);
	void writeLcd(const char *message);
	bool isDisplayOn(void);
};

extern LcdClass Lcd;

#endif
