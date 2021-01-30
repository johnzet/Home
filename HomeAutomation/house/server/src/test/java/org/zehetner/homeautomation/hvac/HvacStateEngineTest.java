package org.zehetner.homeautomation.hvac;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.mock.MockTransceiver;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 11:24 PM
 */
public class HvacStateEngineTest {

    private HvacStateEngine hvacStateEngine = null;
    private HvacMechanical hvacMechanical = null;
    private HvacSettings hvacSettings = null;
    private Sensors sensors = null;
    private HvacSystem hvacSystem = null;

    @Before
    public void before() throws InterruptedException {
        Manager.getSingleton();
        Manager.junitSetDateNow(new Date());
        this.hvacSystem = Manager.getSingleton().getHvacSystem();
        this.hvacMechanical = this.hvacSystem.getHvacStateEngine().getHvacMechanical();
        this.hvacSettings = this.hvacSystem.getHvacStateEngine().getHvacSettings();
        this.sensors = Manager.getSingleton().getSensors();
        this.hvacMechanical.setTransceiver(new MockTransceiver());
        this.hvacStateEngine = this.hvacSystem.getHvacStateEngine();

        this.hvacStateEngine.junitSetLoopDelay(1L);
        this.hvacMechanical.junitSetLoopDelay(1L);
        this.hvacSystem.start();
        Thread.sleep(30L);
    }

    @After
    public void after() throws InterruptedException {
        this.hvacSystem.stop();
        Thread.sleep(30L);
        Manager.junitDeleteManager();
    }

    @Test
    public void testTurnHeat1On() throws InterruptedException {
        this.hvacSettings.setMode(Mode.HEAT);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC + this.hvacSettings.getCelsiusHysteresis() + 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));

    }

    @Test
    public void testTurnHeat2On() throws InterruptedException {
        this.hvacSettings.setMode(Mode.HEAT);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC + this.hvacSettings.getCelsiusSecondStageThreshold() + 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testFan() throws InterruptedException {
        this.hvacSettings.setFanOn(true);
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertTrue(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testLeaveHeat1On() throws InterruptedException {
        this.hvacSettings.setMode(Mode.HEAT);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC + this.hvacSettings.getCelsiusHysteresis() + 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));

        this.hvacSettings.setMode(Mode.HEAT);
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC /*- settings.getCelsiusHysteresis()*/ - 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testTurnHeat1Off() throws InterruptedException {
        this.hvacSettings.setMode(Mode.HEAT);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC - this.hvacSettings.getCelsiusHysteresis() - 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testTurnCool1On() throws InterruptedException {
        this.hvacSettings.setMode(Mode.COOL);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC - this.hvacSettings.getCelsiusHysteresis() - 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertTrue(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testLeaveCool1On() throws InterruptedException {
        this.hvacSettings.setMode(Mode.COOL);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC - this.hvacSettings.getCelsiusHysteresis() - 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertTrue(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testTurnCool1Off() throws InterruptedException {
        this.hvacSettings.setMode(Mode.COOL);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC + this.hvacSettings.getCelsiusHysteresis() + 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testOffModeAndStaysOffCool1Off() throws InterruptedException {
        this.hvacSettings.setMode(Mode.OFF);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC - this.hvacSettings.getCelsiusHysteresis() - 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }


    @Test
    public void testOffModeAndStaysOffHeat1Off() throws InterruptedException {
        this.hvacSettings.setMode(Mode.OFF);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC + this.hvacSettings.getCelsiusHysteresis() + 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testChangeModeToCoolWhileHeatOn() throws InterruptedException {
        this.hvacSettings.setMode(Mode.HEAT);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC + this.hvacSettings.getCelsiusHysteresis() + 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));

        this.hvacSettings.setMode(Mode.COOL);
        Thread.sleep(30L);
        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertTrue(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));

        Manager.junitAdjustDateNow(60L * 60L * 1000L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }

    @Test
    public void testChangeModeToHeatWhileCoolOn() throws InterruptedException {
        this.hvacSettings.setMode(Mode.COOL);
        final double anyTempC = 25.0;
        this.sensors.setIndoorTemperature(new CelsiusTemperature(anyTempC));
        this.hvacSettings.setHoldTemperature(new CelsiusTemperature(anyTempC - this.hvacSettings.getCelsiusHysteresis() - 0.1));
        Thread.sleep(30L);

        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertTrue(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));

        this.hvacSettings.setMode(Mode.HEAT);
        Thread.sleep(30L);
        Manager.junitAdjustDateNow(HvacMechanical.getMechanicalDelay() + 1L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertTrue(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));

        Manager.junitAdjustDateNow(60L * 60L * 1000L);
        Thread.sleep(30L);

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_1));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_1));

        assertFalse(this.hvacMechanical.isOn(Equipment.HEAT_2));
        assertFalse(this.hvacMechanical.isOn(Equipment.COOL_2));

        assertFalse(this.hvacMechanical.isOn(Equipment.FAN));
        assertFalse(this.hvacMechanical.isOn(Equipment.HUMIDIFIER));
    }
}
