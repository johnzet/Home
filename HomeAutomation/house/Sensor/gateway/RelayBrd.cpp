

#include <Arduino.h>
#include <inttypes.h>
#include <Wire.h>
#include "RelayBrd.h"



/******************************************************************************
 * Global Functions
 ******************************************************************************/
void writeLcd(const char *message);


/******************************************************************************
 * Public Functions
 ******************************************************************************/

uint8_t RelayBrdClass::readState(uint8_t address)
{
    Wire.beginTransmission(address >> 1);
    Wire.write(0x01);
    Wire.endTransmission();

    Wire.requestFrom(address >> 1, 1);
	return Wire.read();
	
	//char buffer[50];
	//sprintf(buffer, "Relay Board %x|Access Failed", address);
	//writeLcd(buffer);
	//return 0xFF;
}

void RelayBrdClass::setState(uint8_t address, uint8_t state)
{

    Wire.beginTransmission(address >> 1);
    Wire.write(0x01);
    Wire.write(state);
    Wire.endTransmission();
    delay(10);
}

void RelayBrdClass::changeAddress(uint8_t newAddress) 
{
    Wire.beginTransmission(0x70 >> 1 /* factory default address */);
    Wire.write((uint8_t)0x00);
    Wire.write(0xA0);
    Wire.endTransmission();

    Wire.beginTransmission(0x70 >> 1 /* factory default address */);
    Wire.write((uint8_t)0x00);
    Wire.write(0xAA);
    Wire.endTransmission();

    Wire.beginTransmission(0x70 >> 1 /* factory default address */);
    Wire.write((uint8_t)0x00);
    Wire.write(0xA5);
    Wire.endTransmission();

    Wire.beginTransmission(0x70 >> 1 /* factory default address */);
    Wire.write((uint8_t)0x00);
    Wire.write(newAddress);
    Wire.endTransmission();
}	

RelayBrdClass RelayBrd;
