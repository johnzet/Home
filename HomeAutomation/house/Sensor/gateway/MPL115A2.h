/*
 MPL115A2 Sensor Library for Arduino
   created by R.N <zan73722@gmail.com>

  2011-01-16 Created.
  2011-10-06 Compatibility with Arduino 1.0 <www.misenso.com>
*/

#ifndef MPL115A2_H
#define MPL115A2_H

#include "Arduino.h"

#include <Wire.h>
#include <inttypes.h>

class MPL115A2Class
{
	private:
		int m_shdnPin;
		int m_bShutdown;
		int m_i2c_address;
		signed int sia0, sib1, sib2, sic12, sic11, sic22;
		unsigned int uiPadc, uiTadc;
		bool commError;

	public:
		MPL115A2Class(const int shdnPin = 9);
		void begin();
		void shutdown();
		void ReadSensor();
		float GetPressure();
		float GetTemperature();
};

extern MPL115A2Class MPL115A2;

#endif
