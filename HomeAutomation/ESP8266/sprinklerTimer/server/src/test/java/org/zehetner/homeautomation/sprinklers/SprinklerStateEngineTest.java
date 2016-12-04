package org.zehetner.homeautomation.sprinklers;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.mock.MockHttpServletRequest;
import org.zehetner.homeautomation.mock.MockSprinklerHttpComm;
import org.zehetner.homeautomation.server.responder.SprinklersAjaxResponder;
import org.zehetner.homeautomation.stateengine.ProgramSet;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;

import java.io.IOException;

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
    private long waitTime = 1200L;

    private final String testProgStr = "<list>\n" +
            "  <SprinklerProgram>\n" +
            "    <name>Program A</name>\n" +
            "    <enabled>true</enabled>\n" +
            "    <multiplier>100</multiplier>\n" +
            "    <repeat>\n" +
            "      <type>EVERY_N_DAYS</type>\n" +
            "      <optionalDaysInterval>3</optionalDaysInterval>\n" +
            "      <dayList/>\n" +
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

    @Test
    public void testSetStateThroughThread() throws InterruptedException {
        Manager.junitDeleteManager();
        Manager.getSingleton();

        final Manager manager = Manager.getSingleton();
        final SprinklerMechanical sprinklerMechanical = manager.getSprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = manager.getSprinklerStateEngine();
        sprinklerStateEngine.setCalculationInterval(10L);
        manager.getSprinklerMechanical().setSprinklerHttpComm(new MockSprinklerHttpComm());

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_5);
        sprinklerStateEngine.start();
        Thread.sleep(waitTime);
        sprinklerStateEngine.junitSetRunEnabled(false);

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertTrue(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_7);
        sprinklerStateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        sprinklerStateEngine.junitSetRunEnabled(false);

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertTrue(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        sprinklerStateEngine.interrupt();
        Manager.junitDeleteManager();
    }

    @Test
    public void testWrongState() throws InterruptedException {
        Manager.junitDeleteManager();
        Manager.getSingleton();

        final Manager manager = Manager.getSingleton();
        final SprinklerMechanical sprinklerMechanical = manager.getSprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = manager.getSprinklerStateEngine();
        manager.getSprinklerMechanical().setSprinklerHttpComm(new MockSprinklerHttpComm());

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_5);
        sprinklerStateEngine.setCalculationInterval(10L);
        sprinklerStateEngine.start();
        Thread.sleep(waitTime);
        sprinklerStateEngine.junitSetRunEnabled(false);

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertTrue(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        manager.getSprinklerMechanical().getSprinklerHttpComm().executeGet("/on?zone=7", "");
        sprinklerStateEngine.junitSetRunEnabled(true);
        Thread.sleep(2000L);
        sprinklerStateEngine.junitSetRunEnabled(false);

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertTrue(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        sprinklerStateEngine.interrupt();
        Manager.junitDeleteManager();
    }

    @Test
    public void testOnDemandOnTooLong() throws InterruptedException {
        Manager.junitDeleteManager();
        Manager.getSingleton();

        final Manager manager = Manager.getSingleton();
        final SprinklerMechanical sprinklerMechanical = manager.getSprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = manager.getSprinklerStateEngine();
        sprinklerStateEngine.setCalculationInterval(10L);
        manager.getSprinklerMechanical().setSprinklerHttpComm(new MockSprinklerHttpComm());

        sprinklerStateEngine.setOnDemandZoneState(Zone.ZONE_5);
        sprinklerStateEngine.start();
        Thread.sleep(waitTime);
        sprinklerStateEngine.junitSetRunEnabled(false);

        assertFalse(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertTrue(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        Manager.junitAdjustDateNow(21 * 60 * 1000);
        sprinklerStateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        sprinklerStateEngine.junitSetRunEnabled(false);

        assertTrue(sprinklerMechanical.isZoneOn(Zone.ALL_OFF));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_1));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_2));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_3));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_4));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_5));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_6));
        assertFalse(sprinklerMechanical.isZoneOn (Zone.ZONE_7));
        assertFalse(sprinklerMechanical.isZoneOn(Zone.ZONE_8));

        sprinklerStateEngine.interrupt();
        Manager.junitDeleteManager();
    }

    @Test
    public void testProcessResponseForProgram() throws InterruptedException {

        final SprinklerStateEngine stateEngine = Manager.getSingleton().getSprinklerStateEngine();
        stateEngine.setCalculationInterval(10L);

        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.loadFromXml(testProgStr);
        final SprinklerProgram prog = (SprinklerProgram)progSet.getPrograms().get(0);

        final SprinklerMechanical mechanical = Manager.getSingleton().getSprinklerMechanical();
        Manager.getSingleton().getSprinklerMechanical().setSprinklerHttpComm(new MockSprinklerHttpComm());

        final DateTime timeNow = new DateTime(2016, 1, 3, 10, 10);  // set date to Jan since n_days is referenced to the day of the year.
        Manager.getSingleton().junitSetDateNow(timeNow);
        final DateTime startTime = timeNow.minusMinutes(5);
        prog.setStartTime(new SprinklerProgram.ActivationTime(startTime.getHourOfDay(), startTime.getMinuteOfHour()));

        stateEngine.start();
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);

        assertEquals(Zone.ZONE_3, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ZONE_3));

        // set on-demand override
        stateEngine.setOnDemandZoneState(Zone.ZONE_5);
        stateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);

        assertEquals(Zone.ZONE_5, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ZONE_5));
        stateEngine.setOnDemandZoneState(Zone.ALL_OFF);

        stateEngine.interrupt();
        Manager.junitDeleteManager();
    }

    @Test
    public void testRunProgramNow() throws InterruptedException {

        final SprinklerStateEngine stateEngine = Manager.getSingleton().getSprinklerStateEngine();
        stateEngine.setCalculationInterval(10L);

        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.junitClearPrograms();
        progSet.loadFromXml(testProgStr);
        final SprinklerProgram prog = (SprinklerProgram)progSet.getPrograms().get(0);

        final SprinklerMechanical mechanical = Manager.getSingleton().getSprinklerMechanical();
        mechanical.setSprinklerHttpComm(new MockSprinklerHttpComm());

        final DateTime startTime = Manager.getDateNow().minusMinutes(1);
        prog.setMultiplier(30);
        prog.setOnDemandStartTime(startTime);

        stateEngine.start();
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);

        assertEquals(Zone.ZONE_3, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ZONE_3));

        Manager.junitSetDateNow(Manager.getDateNow().plusMinutes(3));
        stateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);

        assertEquals(Zone.ZONE_7, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ZONE_7));

        stateEngine.interrupt();
        Manager.junitDeleteManager();
    }

    @Test
    public void testProcessResponseForProgramInProgressAfterDisable() throws InterruptedException, IOException, JSONException {

        final SprinklerStateEngine stateEngine = Manager.getSingleton().getSprinklerStateEngine();
        stateEngine.setCalculationInterval(10L);
        stateEngine.junitClearOnDemandZoneState();

        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.loadFromXml(testProgStr);
        final SprinklerProgram prog = (SprinklerProgram)progSet.getPrograms().get(0);

        final DateTime startTime = Manager.getDateNow().minusMinutes(2);
        prog.setStartTime(new SprinklerProgram.ActivationTime(startTime.getHourOfDay(), startTime.getMinuteOfHour()));

        MockSprinklerHttpComm comm = new MockSprinklerHttpComm();
        Manager.getSingleton().getSprinklerMechanical().setSprinklerHttpComm(comm);

        stateEngine.start();
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);
        final SprinklerMechanical mechanical = Manager.getSingleton().getSprinklerMechanical();
        assertEquals(Zone.ZONE_3, comm.junitGetActiveZone());
        assertEquals(Zone.ZONE_3, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ZONE_3));

        // Disable the program as the UI does
        SprinklersAjaxResponder responder = new SprinklersAjaxResponder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("http://localhost/house/sprinklersAjax/setProgramEnable");
        request.getParameterMap().put("programName", new String[]{prog.getName()});
        request.getParameterMap().put("value", new String[]{"false"});
        responder.respond(request);

        stateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);
        assertEquals(Zone.ALL_OFF, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ALL_OFF));
        assertEquals(Zone.ALL_OFF, comm.junitGetActiveZone());

        // Move forward in time
        Manager.junitSetDateNow(Manager.getDateNow().plusMinutes(10));
        stateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);
        assertEquals(Zone.ALL_OFF, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ALL_OFF));
        assertEquals(Zone.ALL_OFF, comm.junitGetActiveZone());

        stateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);
        assertEquals(Zone.ALL_OFF, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ALL_OFF));
        assertEquals(Zone.ALL_OFF, comm.junitGetActiveZone());

        // Artificially set the wrong zone
        comm.junitSetActiveZone(Zone.ZONE_1);
        assertEquals(Zone.ALL_OFF, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ALL_OFF));
        assertEquals(Zone.ZONE_1, comm.junitGetActiveZone());

        stateEngine.junitSetRunEnabled(true);
        Thread.sleep(waitTime);
        stateEngine.junitSetRunEnabled(false);
        assertEquals(Zone.ALL_OFF, mechanical.getActiveZone());
        assertTrue(mechanical.isZoneOn(Zone.ALL_OFF));
        assertEquals(Zone.ALL_OFF, comm.junitGetActiveZone());

        stateEngine.interrupt();
        Manager.junitDeleteManager();
    }
}
