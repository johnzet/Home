package org.zehetner.homeautomation.hvac;

import com.rapplogic.xbee.api.PacketListener;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.junit.Test;
import org.junit.internal.matchers.StringContains;
import org.zehetner.homeautomation.Utils;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.utils.TestUtils;
import org.zehetner.homeautomation.xbee.Transceiver;
import org.zehetner.homeautomation.xbee.XBeeDevice;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 2/19/12
 * Time: 7:17 PM
 */
public class SensorPollerTest {

    @Test
    public void testThermostatMessage() {
        final SensorPoller poller = new SensorPoller();
        final SensorPollerTest.TestTransceiver transceiver =
                new SensorPollerTest.TestTransceiver(
                        Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1));
        poller.setTransceiver(transceiver);
        transceiver.addPacketListener(poller);
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacMechanical().init();

        final String anyHum = "42.02";
        final String anyTemp = "25.01";
        final String anyBat = "75";
        transceiver.simulatePacket(
                XbeeCommandName.SensorData
                        + " " + SensorPoller.TEMPERATURE_C_KEY + ' ' + anyTemp
                        + " |" + SensorPoller.HUMIDITY_KEY + ' ' + anyHum
                        + " |" + SensorPoller.BAT_PERCENT_KEY + ' ' + anyBat
        );

        final String returnPacket = transceiver.getPayload();

        assertThat(returnPacket, StringContains.containsString(XbeeCommandName.LcdText.name()));
        assertThat(returnPacket, StringContains.containsString("|"));

        final Sensors sensors = Manager.getSingleton().getSensors();
        assertTrue(TestUtils.closeEnough(25.01, sensors.getIndoorTemperature().getCelsiusTemperature()));
        assertTrue(TestUtils.closeEnough(42.02, sensors.getIndoorHumidity()));
        assertTrue(TestUtils.closeEnough(75.0, sensors.getThermostat1BatPercent()));
    }

    @Test
    public void testThermostatMessageLowBat() {
        final SensorPoller poller = new SensorPoller();
        final SensorPollerTest.TestTransceiver transceiver =
                new SensorPollerTest.TestTransceiver(
                        Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1));
        poller.setTransceiver(transceiver);
        transceiver.addPacketListener(poller);
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacMechanical().init();

        final String anyHum = "42.02";
        final String anyTemp = "25.01";
        final String anyLowBat = "4";
        for (int i=0; i<20; i++) {
            // battery readings are smoothed with a moving window average
            transceiver.simulatePacket(
                    XbeeCommandName.SensorData
                            + " " + SensorPoller.TEMPERATURE_C_KEY + ' ' + anyTemp
                            + " |" + SensorPoller.HUMIDITY_KEY + ' ' + anyHum
                            + " |" + SensorPoller.BAT_PERCENT_KEY + ' ' + anyLowBat
            );
        }

        final String returnPacket = transceiver.getPayload();

        assertThat(returnPacket, StringContains.containsString(XbeeCommandName.LcdTextBeep.name()));
        assertThat(returnPacket, StringContains.containsString("|"));

        final Sensors sensors = Manager.getSingleton().getSensors();
        assertTrue(TestUtils.closeEnough(25.01, sensors.getIndoorTemperature().getCelsiusTemperature()));
        assertTrue(TestUtils.closeEnough(42.02, sensors.getIndoorHumidity()));
        assertTrue(TestUtils.closeEnough(4.0, sensors.getThermostat1BatPercent()));
    }

    @Test
    public void testThermostatTempUp() {
        final SensorPoller poller = new SensorPoller();
        final SensorPollerTest.TestTransceiver transceiver =
                new SensorPollerTest.TestTransceiver(
                        Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1));
        poller.setTransceiver(transceiver);
        transceiver.addPacketListener(poller);
        final HvacSettings settings = Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings();
        final Temperature initialTemp = settings.getHoldTemperature();
        transceiver.simulatePacket(
                XbeeCommandName.TemperatureChange
                        + " 2"
        );
        final Temperature newTemp = settings.getHoldTemperature();

        assertTrue(TestUtils.closeEnough(2.0, newTemp.getFahrenheitTemperature() - initialTemp.getFahrenheitTemperature()));
    }

    @Test
    public void testThermostatTempDown() {
        final SensorPoller poller = new SensorPoller();
        final SensorPollerTest.TestTransceiver transceiver =
                new SensorPollerTest.TestTransceiver(
                        Utils.get64BitAddress(XBeeDevice.THERMOSTAT_1));
        poller.setTransceiver(transceiver);
        transceiver.addPacketListener(poller);
        final HvacSettings settings = Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings();
        final Temperature initialTemp = settings.getHoldTemperature();
        transceiver.simulatePacket(
                XbeeCommandName.TemperatureChange
                        + " -1"
        );
        final Temperature newTemp = settings.getHoldTemperature();

        assertTrue(TestUtils.closeEnough(1.0, initialTemp.getFahrenheitTemperature() - newTemp.getFahrenheitTemperature()));
    }

    @Test
    public void testGatewayMessage() {
        final SensorPoller poller = new SensorPoller();
        final SensorPollerTest.TestTransceiver transceiver =
                new SensorPollerTest.TestTransceiver(
                        Utils.get64BitAddress(XBeeDevice.GATEWAY));
        poller.setTransceiver(transceiver);
        transceiver.addPacketListener(poller);
        final String anyHum = "42.02";
        final String anyTemp = "25.01";
        transceiver.simulatePacket(
                XbeeCommandName.SensorData
              + " " + SensorPoller.TEMPERATURE_C_KEY + ' ' + anyTemp
              + " |" + SensorPoller.HUMIDITY_KEY + ' ' + anyHum
              + " |" + SensorPoller.BAROMETER_KEY + ' ' + "83.76" //kPa
              + " |"
        );

        final String returnPacket = transceiver.getPayload();

        assertThat(returnPacket, StringContains.containsString(XbeeCommandName.LcdText.name()));
        assertThat(returnPacket, StringContains.containsString("|"));

        final Sensors sensors = Manager.getSingleton().getSensors();
        assertTrue(TestUtils.closeEnough(25.01, sensors.getOutdoorTemperature().getCelsiusTemperature()));
        assertTrue(TestUtils.closeEnough(42.02, sensors.getOutdoorHumidity()));
        assertTrue(TestUtils.closeEnough(29.98, sensors.getBarometer()));

    }

    private class TestTransceiver implements Transceiver {
        private final XBeeAddress64 address64;
        private PacketListener packetListener = null;
        private String payload = null;

        TestTransceiver(final XBeeAddress64 address64Arg) {
            this.address64 = address64Arg;
        }

        @Override
        public void sendRequest(final XBeeAddress64 address64, final String payloadArg) {
            this.payload = payloadArg;
        }

        @Override
        public void addPacketListener(final PacketListener packetListenerArg) {
            this.packetListener = packetListenerArg;
        }

        @Override
        public void initTransceiver() {
            //To change body of implemented methods use File | HvacSettings | File Templates.
        }

        public void simulatePacket(final String s) {
            final ZNetRxResponse resp = new ZNetRxResponse();
            resp.setRemoteAddress64(this.address64);
            resp.setData(ByteUtils.stringToIntArray(s));
            this.packetListener.processResponse(resp);
        }

        public String getPayload() {
            return this.payload;
        }
    }
}
