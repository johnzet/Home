#include "SensorUtils.h"

SensorUtils::SensorUtils(XBee *xbee) {
	this->xbee = xbee;
	this->atResponse = AtCommandResponse();
	this->atRequest = AtCommandRequest();
}

void SensorUtils::sleepXbeeImmediately(void) {
	uint8_t siCmd[] = {'S','I'};
	this->atRequest.setCommand(siCmd);
	this->atRequest.setFrameId(0);  // disable status responses
	this->xbee->send(this->atRequest);
}

uint8_t SensorUtils::getRSSI(void) {
	// Returns the RSSI in -DBm.
	uint8_t dbCmd[] = {'D','B'};
	this->atRequest.setCommand(dbCmd);
	this->atRequest.setFrameId(1); // make sure the frameid > 0 so we get a command response
	this->xbee->send(this->atRequest);

	delay(5);
	
	uint8_t *rssi = NULL;
	int length = this->receiveLocalAtResponse(rssi);
	if (length >= 1) {
		return rssi[0];
	}
	return 0;
}

void SensorUtils::sendRequest(XBeeAddress64 *addr64, uint8_t *msg, int length) {
	ZBTxRequest zbTx = ZBTxRequest(*addr64, msg, length);
	//xbee.getNextFrameId(); // make sure the frameid > 0 so we get a status response
	zbTx.setFrameId(0);  // disable status responses
	this->xbee->send(zbTx);
	
	//receiveStatusResponse();
}

int SensorUtils::receiveLocalAtResponse(uint8_t *response) {
	bool timedOut = false;
	do {
		if (this->xbee->readPacket(200)) {
			if (this->xbee->getResponse().getApiId() == AT_COMMAND_RESPONSE) {
				this->xbee->getResponse().getAtCommandResponse(this->atResponse);
				if (this->atResponse.isOk()) {
					response = this->atResponse.getValue();
					return this->atResponse.getValueLength();
				}					
			} else {
				// There is something else waiting - get it and ignore it.
			}
		} else {
			timedOut = true;
		}
	} while (!timedOut);
	return 0;				
}

//bool receiveStatusResponse() {
	//if (xbee.readPacket(XBEE_STATUS_TIMEOUT)) {
		//if (xbee.getResponse().getApiId() == ZB_TX_STATUS_RESPONSE) {
			//xbee.getResponse().getZBTxStatusResponse(txStatus);
			//if (txStatus.getDeliveryStatus() == SUCCESS) {
				//return true;
			//} else {
				//writeLcd("Bad Tx Status");
			//}
		//} else {
			//writeLcd("Unexpected Packet|Expected Tx Status");
		//}
	//} else {
		//writeLcd("Tx Status Timeout|Is the Server Down?");
	//}
	//return false;
//}