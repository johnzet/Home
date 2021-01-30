package org.zehetner.homeautomation.xbee;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/13/12
 * Time: 10:40 AM
 */
public enum XBeeDevice {

    GATEWAY (XBeeTransceiver.GATEWAY_XBEE_ADDRESS_PROP),
    THERMOSTAT_1 (XBeeTransceiver.THERMOSTAT_1_XBEE_ADDRESS_PROP),
    THERMOSTAT_2 (XBeeTransceiver.THERMOSTAT_2_XBEE_ADDRESS_PROP);

    private String key;

    XBeeDevice(final String keyArg) {
        this.key = keyArg;
    }

    public String getKey() {
        return this.key;
    }
}
