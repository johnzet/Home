/*
 * Lcd.cpp
 *
 * Created: 3/10/2012 8:59:38 AM
 *  Author: johnz
 */

#include <Arduino.h>
#include <inttypes.h>
#include <avr/io.h>
#include <util/delay.h>
#include "Lcd.h"

LcdClass::LcdClass(void) {
	strcpy(lcdBuffer, "");
	strcpy(lcdText, "");
}

void LcdClass::displayOn(void)
{
	// Disable the SPI peripheral since we're bit-banging
	SPCR = 0b00011100;

	digitalWrite(MOSI, LOW);
	digitalWrite(SS, LOW);
	digitalWrite(SCK, LOW);
	digitalWrite(MISO, LOW);

	pinMode(MOSI, OUTPUT);
	pinMode(SS, OUTPUT);
	pinMode(SCK, OUTPUT);
	pinMode(MISO, INPUT);

 	digitalWrite(MISO, LOW);
 	digitalWrite(SS, HIGH);
 	digitalWrite(SCK, HIGH);


	uint16_t data;

    // Wait for power stabilization
	_delay_ms(2);

    // Slave Select Active
 	digitalWrite(SS, LOW);
    _delay_us(1);
	
	// Function Set
	data = 0b0000111000;
	sendWord(data, 10);
	
	_delay_us(600);

	// Display OFF
	data = 0b0000001000;
	sendWord(data, 10);
	_delay_us(600);

	// Clear Display
	data = 0b0000000001;
	sendWord(data, 10);
	_delay_ms(2);

	// Set Entry Mode
	data = 0b0000000110;
	sendWord(data, 10);
	_delay_us(600);

	// Return Home
	data = 0b0000000010;
	sendWord(data, 10);
	_delay_us(600);

	// Display ON
	data = 0b0000001100;
	sendWord(data, 10);
	_delay_us(600);

	// Set DDRAM address
	data = 0b0010000000;
	sendWord(data, 10);
	_delay_us(600);

    // Slave Select Inactive	
 	digitalWrite(SS, HIGH);
    _delay_us(1);

	displayIsOn = true;
	
	if (strlen(lcdText) > 0) {
		_writeLcd(lcdText);
	}	
}

void LcdClass::displayOff(void) {
	//// Slave Select Active
	//_setLow(SPI_PORT, SS);
	//_delay_us(1);
	//
	//// Display OFF
	//uint16_t data = 0b0000001000;
	//sendWord(data, 10);
	//_delay_us(600);
//
	//// Slave Select Inactive
	//_setHigh(SPI_PORT, SS);
	//_delay_us(1);
	
	pinMode(MOSI, INPUT);
	pinMode(SS, INPUT);
	pinMode(SCK, INPUT);
	digitalWrite(MOSI, LOW);
	digitalWrite(SS, LOW);
	digitalWrite(SCK, LOW);

	displayIsOn = false;
}

bool LcdClass::isDisplayOn(void) {
	return displayIsOn;
}


void LcdClass::writeLcd(const char *message) {
	strcpy(lcdText, message);
	
	_writeLcd(lcdText);
}

void LcdClass::_writeLcd(const char *message) {
	
	if (displayIsOn) {

		Lcd.clearDisplay();
		Lcd.clearDisplay(); // I don't know why 2 calls are necessary

		strcpy(lcdBuffer, message);
	
		uint8_t lineNumber = 1;
		char *pch = strtok (lcdBuffer,"|");
		while (pch != NULL) {
			Lcd.writeString(pch, lineNumber++);
			delay(50);
			pch = strtok (NULL, "|");
		}
	
		delay(200);
	}		
}

void LcdClass::clearDisplay(void) {
    // Slave Select Active	
 	digitalWrite(SS, LOW);
    _delay_us(1);

	// Clear Display
	uint16_t data = 0b0000000001;
	sendWord(data, 10);
	_delay_ms(2);

    // Slave Select Inactive	
 	digitalWrite(SS, HIGH);
    _delay_us(1);
}


void LcdClass::writeString(char *text, uint8_t lineNumber /* 1-based*/)
{
	// Line 1 = Address 0x00 through 0x13
    // Line 2 = Address 0x40 through 0x53
    // Line 3 = Address 0x14 through 0x27
    // Line 4 = Address 0x54 through 0x67

	uint16_t address;
	switch(lineNumber) {
		case(1):
		    address = 0x00;
			break;
		case(2):
		    address = 0x40;
			break;
		case(3):
		    address = 0x14;
			break;
		case(4):
		    address = 0x54;
			break;
		default:
		    return;
			break;
	}

    // Slave Select Active	
 	digitalWrite(SS, LOW);

	// Set DDRAM address
	uint16_t data = 0b0010000000 | address;
	sendWord(data, 10);

    // Slave Select Inactive	
 	digitalWrite(SS, HIGH);

    // Slave Select Active	
 	digitalWrite(SS, LOW);

    for (uint8_t i=0; i<strlen(text); i++) {
		if (i == 0)  {
			data = 0b1000000000;
    	    data |= *(text+i);
    	    sendWord(data, 10);
		} else {
	        data = *(text+i);
	        sendWord(data, 8);
		}						
	}
	
    // Slave Select Inactive	
 	digitalWrite(SS, HIGH);
}

void LcdClass::sendWord(uint16_t data, uint8_t length) {
	digitalWrite(SCK, HIGH);

	for (uint8_t i=length; i>0; i--) {
		
		uint16_t oneBit = (data & (1 << (i-1)));
		digitalWrite(SCK, LOW);
		if (oneBit) {
			digitalWrite(MOSI, HIGH);
		} else {
			digitalWrite(MOSI, LOW);
		}
    	digitalWrite(SCK, HIGH);
	}
}

LcdClass Lcd;
