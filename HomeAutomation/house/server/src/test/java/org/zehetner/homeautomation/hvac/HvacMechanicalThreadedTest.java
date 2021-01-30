package org.zehetner.homeautomation.hvac;

import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.util.ByteUtils;
import org.junit.Test;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.XbeeCommandName;
import org.zehetner.homeautomation.mock.MockTransceiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 1/26/12
 * Time: 9:58 PM
 */
public class HvacMechanicalThreadedTest {

    @Test
    public void testHvacRelayMappingProperties() {
        final CombinedProperties properties = Manager.getSingleton().getProperties();

        assertTrue(Integer.parseInt(properties.getSystemProperty(HvacMechanical.HEAT_MIN_TIME_ON)) > 10000);
        assertTrue(Integer.parseInt(properties.getSystemProperty(HvacMechanical.HEAT_MIN_TIME_OFF)) > 10000);
        assertTrue(Integer.parseInt(properties.getSystemProperty(HvacMechanical.COOL_MIN_TIME_ON)) > 10000);
        assertTrue(Integer.parseInt(properties.getSystemProperty(HvacMechanical.COOL_MIN_TIME_OFF)) > 10000);
    }

    @Test
    public void testHeat1() throws InterruptedException {
        runTestHeatAndCoolStateTransitionTiming(Equipment.HEAT_1);
    }

    @Test
    public void testHeat2() throws InterruptedException {
        runTestHeatAndCoolStateTransitionTiming(Equipment.HEAT_2);
    }

    @Test
    public void testCool1() throws InterruptedException {
        runTestHeatAndCoolStateTransitionTiming(Equipment.COOL_1);
    }

    @Test
    public void testCool2() throws InterruptedException {
        runTestHeatAndCoolStateTransitionTiming(Equipment.COOL_2);
    }

    private void runTestHeatAndCoolStateTransitionTiming(final Equipment equipment) throws InterruptedException {
        final HvacMechanical hvacMechanical = new HvacMechanical();
        hvacMechanical.init();
        hvacMechanical.setTransceiver(new MockTransceiver());
        hvacMechanical.junitSetLoopDelay(1L);

        // turn on then off quickly
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));
        setHeatOrCoolState(hvacMechanical, equipment, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));
        setHeatOrCoolState(hvacMechanical, equipment, false);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(60L * 60L * 1000L, hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));

        // turn on
        setHeatOrCoolState(hvacMechanical, equipment, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));

        // turn off
        adjustTimeThenRunWorkerThread(60L * 60L * 1000L, hvacMechanical);
        setHeatOrCoolState(hvacMechanical, equipment, false);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay() + 1L, hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));

        // turn on again too soon, then wait (mechanical delay test)
        Manager.junitAdjustDateNow(60L * 60L * 1000L);
        setHeatOrCoolState(hvacMechanical, equipment, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));

        // turn off then on quickly  (mechanical delay test)
        Manager.junitAdjustDateNow(60L * 60L * 1000L);
        setHeatOrCoolState(hvacMechanical, equipment, false);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));
        setHeatOrCoolState(hvacMechanical, equipment, true);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));

        //  turn off before min time on
        setHeatOrCoolState(hvacMechanical, equipment, false);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(60L * 60L * 1000L, hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));

        //  turn on before min time off
        setHeatOrCoolState(hvacMechanical, equipment, true);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertFalse(isHeatOrCoolOn(hvacMechanical, equipment));
        adjustTimeThenRunWorkerThread(60L * 60L * 1000L, hvacMechanical);
        assertTrue(isHeatOrCoolOn(hvacMechanical, equipment));
    }

    private void runWorkerThread(final Worker worker) throws InterruptedException {
        worker.start();
        Thread.sleep(10L);
        worker.stop();
        Thread.sleep(10L);
    }

    private void adjustTimeThenRunWorkerThread(final long time, final Worker worker) throws InterruptedException {
        Manager.junitAdjustDateNow(time);
        runWorkerThread(worker);
    }

    private void setHeatOrCoolState(final HvacMechanical hvacMechanical, final Equipment equipment, final boolean state) {
        hvacMechanical.setOnByProposal(equipment, state);
    }

    private boolean isHeatOrCoolOn(final HvacMechanical hvacMechanical, final Equipment equipment) {
        return hvacMechanical.isOn(equipment);
    }

    @Test
    public void testFan() throws InterruptedException {
        final HvacMechanical hvacMechanical = new HvacMechanical();
        hvacMechanical.setTransceiver(new MockTransceiver());
        hvacMechanical.init();

        // turn on
        hvacMechanical.setOnByProposal(Equipment.FAN, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.FAN));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(hvacMechanical.isOn(Equipment.FAN));

        // turn off
        hvacMechanical.setOnByProposal(Equipment.FAN, false);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertTrue(hvacMechanical.isOn(Equipment.FAN));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.FAN));

        // turn on then off quickly
        hvacMechanical.setOnByProposal(Equipment.FAN, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.FAN));
        hvacMechanical.setOnByProposal(Equipment.FAN, false);
        adjustTimeThenRunWorkerThread(50000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.FAN));
        adjustTimeThenRunWorkerThread(50000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.FAN));
    }

    @Test
    public void testHumidifier() throws InterruptedException {
        final HvacMechanical hvacMechanical = new HvacMechanical();
        hvacMechanical.setTransceiver(new MockTransceiver());
        hvacMechanical.init();

        // turn on
        hvacMechanical.setOnByProposal(Equipment.HUMIDIFIER, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.HUMIDIFIER));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertTrue(hvacMechanical.isOn(Equipment.HUMIDIFIER));

        // turn off
        hvacMechanical.setOnByProposal(Equipment.HUMIDIFIER, false);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertTrue(hvacMechanical.isOn(Equipment.HUMIDIFIER));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.HUMIDIFIER));

        // turn on then off quickly
        hvacMechanical.setOnByProposal(Equipment.HUMIDIFIER, true);
        adjustTimeThenRunWorkerThread(1000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.HUMIDIFIER));
        hvacMechanical.setOnByProposal(Equipment.HUMIDIFIER, false);
        adjustTimeThenRunWorkerThread(50000L, hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.HUMIDIFIER));
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), hvacMechanical);
        assertFalse(hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testProcessResponse() throws InterruptedException {
        final HvacMechanical mechanical = new HvacMechanical();
        mechanical.init();
        final MockTransceiver transceiver = new MockTransceiver();
        mechanical.setTransceiver(transceiver);

        final ZNetRxResponse resp = new ZNetRxResponse();
        resp.setData(ByteUtils.stringToIntArray(XbeeCommandName.HvacRelayState + " 255"));
        mechanical.processResponse(resp);

        mechanical.start();
        Thread.sleep(10L);
        mechanical.stop();

        final String payload = transceiver.getPayload();

        assertEquals(XbeeCommandName.HvacRelayState + " 0", payload);
    }
}
