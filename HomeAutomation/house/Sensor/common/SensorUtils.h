#ifndef SensorUtils_H
#define SensorUtils_H

#include <Arduino.h>
#include "XBee.h"

class SensorUtils
{
	private:
		XBee *xbee;
		AtCommandResponse atResponse;
		AtCommandRequest atRequest;
		int receiveLocalAtResponse(uint8_t *response);
		
	public:
		SensorUtils(XBee *xbee);
		void sendRequest(XBeeAddress64 *addr64, uint8_t *msg, int length);
		void sleepXbeeImmediately(void);
		uint8_t getRSSI(void);
};

#endif
