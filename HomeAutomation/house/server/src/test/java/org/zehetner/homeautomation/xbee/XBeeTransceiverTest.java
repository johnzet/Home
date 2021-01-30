package org.zehetner.homeautomation.xbee;

import org.junit.Test;
import org.zehetner.homeautomation.common.CombinedProperties;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 2/1/12
 * Time: 7:26 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public class XBeeTransceiverTest {

    @Test
    public void testProperties() {
        final CombinedProperties properties = CombinedProperties.getSingleton();
        assertEquals(23L, (long) properties.getSystemProperty(XBeeTransceiver.GATEWAY_XBEE_ADDRESS_PROP).trim().length());
        assertEquals(23L, (long) properties.getSystemProperty(XBeeTransceiver.THERMOSTAT_1_XBEE_ADDRESS_PROP).trim().length());
        assertEquals("Gateway", properties.getSystemProperty(XBeeTransceiver.GATEWAY_XBEE_NAME_PROP).trim());
        assertEquals("Thermostat 1", properties.getSystemProperty(XBeeTransceiver.THERMOSTAT_1_XBEE_NAME_PROP).trim());

    }

    @Test
    public void testConstants() {
        assertEquals("SET_HVAC_RELAYS", XBeeTransceiver.HVAC_RELAY_BOARD_COMMAND);
        assertEquals("SET_SPRINKLER_RELAYS", XBeeTransceiver.SPRINKLER_RELAY_BOARD_COMMAND);
    }
}
