/*
 MPL115A2 Sensor Library for Arduino
   created by R.N <zan73722@gmail.com>

  2011-01-16 Created.
  2011-10-06 Compatibility with Arduino 1.0 <www.misenso.com>
*/

/******************************************************************************
 * Includes
 ******************************************************************************/
#include "Arduino.h"
#include "MPL115A2.h"

/******************************************************************************
 * Constructors
 ******************************************************************************/
MPL115A2Class::MPL115A2Class(const int shdnPin)
{
	commError = false;
	m_i2c_address = 0x60;
	m_shdnPin = shdnPin;
	//Wire.begin();
}

/******************************************************************************
 * Global Functions
 ******************************************************************************/

void MPL115A2Class::ReadSensor()
{
	short uiPH = 0, uiPL = 0, uiTH = 0, uiTL = 0;

	//wakeup
    digitalWrite(m_shdnPin, HIGH);
    m_bShutdown = 0;
    delay(1); // wait the device to be ready

	// start AD conversions
    Wire.beginTransmission(m_i2c_address);
    Wire.write(0x12);
    Wire.write(0x01);
    Wire.endTransmission();
    delay(10);

    Wire.beginTransmission(m_i2c_address);
    Wire.write((uint8_t) 0x00);
    Wire.endTransmission();
    Wire.requestFrom(m_i2c_address, 4);

	commError = (Wire.available() <= 0);
    if (!commError)
	{
		uiPH = Wire.read();
		uiPL = Wire.read();
		uiTH = Wire.read();
		uiTL = Wire.read();
    }

	uiPadc = (unsigned int) uiPH << 8;
	uiPadc += (unsigned int) uiPL & 0x00FF;
	uiTadc = (unsigned int) uiTH << 8;
	uiTadc += (unsigned int) uiTL & 0x00FF;

	// Coefficient 9 equation compensation
	uiPadc = uiPadc >> 6;
	uiTadc = uiTadc >> 6;
}


/**********************************************************
 * GetPressure
 *  Gets the current pressure from the sensor.
 *
 * @return float - The local pressure in kPa
 **********************************************************/
float MPL115A2Class::GetPressure()
{
	if (commError) {
		return -1.0;
	}
	
	signed int siPcomp;
	signed long lt1, lt2, lt3, si_c11x1, si_a11, si_c12x2;
	signed long si_a1, si_c22x2, si_a2, si_a1x1, si_y1, si_a2x2;

	// Step 1 c11x1 = c11 * Padc
	lt1 = (signed long) sic11;
	lt2 = (signed long) uiPadc;
	lt3 = lt1*lt2;
	si_c11x1 = (signed long) lt3;

	// Step 2 a11 = b1 + c11x1
	lt1 = ((signed long)sib1)<<14;
	lt2 = (signed long) si_c11x1;
	lt3 = lt1 + lt2;
	si_a11 = (signed long)(lt3>>14);

	// Step 3 c12x2 = c12 * Tadc
	lt1 = (signed long) sic12;
	lt2 = (signed long) uiTadc;
	lt3 = lt1*lt2;
	si_c12x2 = (signed long)lt3;

	// Step 4 a1 = a11 + c12x2
	lt1 = ((signed long)si_a11<<11);
	lt2 = (signed long)si_c12x2;
	lt3 = lt1 + lt2;
	si_a1 = (signed long) lt3>>11;

	// Step 5 c22x2 = c22*Tadc
	lt1 = (signed long)sic22;
	lt2 = (signed long)uiTadc;
	lt3 = lt1 * lt2;
	si_c22x2 = (signed long)(lt3);

	// Step 6 a2 = b2 + c22x2
	lt1 = ((signed long)sib2<<15);
	lt2 = ((signed long)si_c22x2>1);
	lt3 = lt1+lt2;
	si_a2 = ((signed long)lt3>>16);

	// Step 7 a1x1 = a1 * Padc
	lt1 = (signed long)si_a1;
	lt2 = (signed long)uiPadc;
	lt3 = lt1*lt2;
	si_a1x1 = (signed long)(lt3);

	// Step 8 y1 = a0 + a1x1
	lt1 = ((signed long)sia0<<10);
	lt2 = (signed long)si_a1x1;
	lt3 = lt1+lt2;
	si_y1 = ((signed long)lt3>>10);

	// Step 9 a2x2 = a2 * Tadc
	lt1 = (signed long)si_a2;
	lt2 = (signed long)uiTadc;
	lt3 = lt1*lt2;
	si_a2x2 = (signed long)(lt3);

	// Step 10 pComp = y1 + a2x2
	lt1 = ((signed long)si_y1<<10);
	lt2 = (signed long)si_a2x2;
	lt3 = lt1+lt2;

	// Fixed point result with rounding
	siPcomp = ((signed long)lt3>>13);

	// decPcomp is defined as a floating point number
	// Conversion to decimal value from 1023 ADC count value
	// ADC counts are 0 to 1023, pressure is 50 to 115kPa respectively
	return (((65.0/1023.0)*siPcomp)+50);
}


/**********************************************************
 * GetTemperature
 *  Gets the current temperature from the sensor.
 *
 * @return float - The local pressure in °C
 **********************************************************/
float MPL115A2Class::GetTemperature()
{
	return (25 + ((uiTadc - 498.0) / -5.35));
}


/**********************************************************
 * Begin
 *  Gets the coefficients from the sensor.
 *
 **********************************************************/
void MPL115A2Class::begin()
{
	short sia0MSB = 0, sia0LSB = 0;
	short sib1MSB = 0, sib1LSB = 0;
	short sib2MSB = 0, sib2LSB = 0;
	short sic12MSB = 0, sic12LSB = 0;
	short sic11MSB = 0, sic11LSB = 0;
	short sic22MSB = 0, sic22LSB = 0;

	//wakeup
    pinMode(m_shdnPin, OUTPUT);
    digitalWrite(m_shdnPin, HIGH);
    m_bShutdown = 0;
    delay(1); // wait the device to be ready

    //send address
    Wire.beginTransmission(m_i2c_address);
    Wire.write(0x04);
    Wire.endTransmission();

    // read out coefficients
    Wire.requestFrom(m_i2c_address, 12);
	
    if (Wire.available() >= 12)
	{
		sia0MSB = Wire.read();
		sia0LSB = Wire.read();
		sib1MSB = Wire.read();
		sib1LSB = Wire.read();
		sib2MSB = Wire.read();
		sib2LSB = Wire.read();
		sic12MSB = Wire.read();
		sic12LSB = Wire.read();
		sic11MSB = Wire.read();
		sic11LSB = Wire.read();
		sic22MSB = Wire.read();
		sic22LSB = Wire.read();
    }

	// Placing coefficients into 16-bit Variables
	// a0
	sia0 = (signed int) sia0MSB << 8;
	sia0 += (signed int) sia0LSB & 0x00FF;
	// b1
	sib1 = (signed int) sib1MSB << 8;
	sib1 += (signed int) sib1LSB & 0x00FF;
	// b2
	sib2 = (signed int) sib2MSB << 8;
	sib2 += (signed int) sib2LSB & 0x00FF;
	// c12
	sic12 = (signed int) sic12MSB << 8;
	sic12 += (signed int) sic12LSB & 0x00FF;
	// c11
	sic11 = (signed int) sic11MSB << 8;
	sic11 += (signed int) sic11LSB & 0x00FF;
	// c22
	sic22 = (signed int) sic22MSB << 8;
	sic22 += (signed int) sic22LSB & 0x00FF;
}


void MPL115A2Class::shutdown()
{
	digitalWrite(m_shdnPin, LOW);
	m_bShutdown = 1;
}

MPL115A2Class MPL115A2;
