package org.zehetner.homeautomation.sprinklers;

import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.mock.MockTransceiver;
import org.zehetner.homeautomation.stateengine.ProgramSet;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;
import org.zehetner.homeautomation.xbee.Transceiver;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnzet
 * Date: 2/18/12
 * Time: 10:39 PM
 */
public class SprinklerStateEngineTest {
    @Test
    public void testSetStateThroughThread() throws InterruptedException {
        Manager.junitDeleteManager();
        Manager.getSingleton();

        final Manager manager = Manager.getSingleton();
        final SprinklerMechanical sprinklerMechanical = manager.getSprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = manager.getSprinklerStateEngine();
        sprinklerMechanical.setTransceiver(new MockTransceiver());

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_5);
        sprinklerStateEngine.start();
        Thread.sleep(10L);
        sprinklerStateEngine.stop();

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertTrue(sprinklerMechanical.isZoneOn (Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_7);
        sprinklerStateEngine.start();
        Thread.sleep(10L);
        sprinklerStateEngine.stop();

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertTrue(sprinklerMechanical.isZoneOn (Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));
    }

    @Test
    public void testOnDemandOnTooLong() throws InterruptedException {
        Manager.junitDeleteManager();
        Manager.getSingleton();

        final Manager manager = Manager.getSingleton();
        final SprinklerMechanical sprinklerMechanical = manager.getSprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = manager.getSprinklerStateEngine();
        sprinklerMechanical.setTransceiver(new MockTransceiver());

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_5);
        sprinklerStateEngine.start();
        Thread.sleep(10L);
        sprinklerStateEngine.stop();

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertTrue(sprinklerMechanical.isZoneOn (Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        Manager.junitAdjustDateNow(11 * 60 * 1000);
        sprinklerStateEngine.start();
        Thread.sleep(10L);
        sprinklerStateEngine.stop();

        assertTrue(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn (Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));
    }

    @Test
    public void testProcessResponseWrongState() throws InterruptedException {
        final MockTransceiver transceiver = new MockTransceiver();
        final SprinklerMechanical mechanical = Manager.getSingleton().getSprinklerMechanical();
        mechanical.setTransceiver(transceiver);

        final SprinklerStateEngine stateEngine = Manager.getSingleton().getSprinklerStateEngine();
        stateEngine.setOnDemandZoneState(Zone.ALL_OFF);
        stateEngine.start();
        Thread.sleep(10L);
        stateEngine.stop();

        final ZNetRxResponse resp = new ZNetRxResponse();
        resp.setData(ByteUtils.stringToIntArray(XbeeCommandName.SprinklerRelayState + " 255"));
        mechanical.processResponse(resp);

        stateEngine.setOnDemandZoneState(Zone.ALL_OFF);
        stateEngine.start();
        Thread.sleep(10L);
        stateEngine.stop();
        Thread.sleep(10L);

        final String payload = transceiver.getPayload();

        assertEquals(XbeeCommandName.SprinklerRelayState + " 0", payload);


    }

    @Test
    public void testProcessResponseCorrectState() throws InterruptedException {
        final SprinklerMechanical mechanical = Manager.getSingleton().getSprinklerMechanical();
        final MockTransceiver transceiver = new MockTransceiver();
        mechanical.setTransceiver(transceiver);

        final ZNetRxResponse resp = new ZNetRxResponse();
        resp.setData(ByteUtils.stringToIntArray(XbeeCommandName.SprinklerRelayState + " 255"));
        mechanical.processResponse(resp);

        final SprinklerStateEngine stateEngine = Manager.getSingleton().getSprinklerStateEngine();
        stateEngine.start();
        Thread.sleep(10L);
        stateEngine.stop();

        final String payload = transceiver.getPayload();

        assertEquals(XbeeCommandName.SprinklerRelayState + " 0", payload);
    }

    @Test
    public void testProcessResponseForProgram() throws InterruptedException {
        final String testProgStr = "<list>\n" +
                "  <SprinklerProgram>\n" +
                "    <name>Program A</name>\n" +
                "    <repeat>\n" +
                "      <type>EVERY_THREE_DAYS</type>\n" +
                "      <dayList/>\n" +
                "      <recentCompletion>1970-01-01 00:00:00.0 UTC</recentCompletion>\n" +
                "    </repeat>\n" +
                "    <startTime>\n" +
                "      <hour>4</hour>\n" +
                "      <minute>30</minute>\n" +
                "    </startTime>\n" +
                "    <actions>\n" +
                "      <SprinklerAction>\n" +
                "        <durationMinutes>11</durationMinutes>\n" +
                "        <zone>ZONE_3</zone>\n" +
                "      </SprinklerAction>\n" +
                "      <SprinklerAction>\n" +
                "        <durationMinutes>15</durationMinutes>\n" +
                "        <zone>ZONE_7</zone>\n" +
                "      </SprinklerAction>\n" +
                "    </actions>\n" +
                "  </SprinklerProgram>\n" +
                "</list>";

        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.loadFromXml(testProgStr);
        final SprinklerProgram prog = (SprinklerProgram)progSet.getPrograms().get(0);

        final SprinklerMechanical mechanical = Manager.getSingleton().getSprinklerMechanical();
        final Transceiver transceiver = new MockTransceiver();
        mechanical.setTransceiver(transceiver);

        final DateTime dateTime = new DateTime().minusMinutes(5);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new Date());

        final SprinklerStateEngine stateEngine = Manager.getSingleton().getSprinklerStateEngine();
        stateEngine.start();
        Thread.sleep(100L);
        stateEngine.stop();

        assertTrue(mechanical.isZoneOn(Zone.ZONE_3));
    }

}
