package org.zehetner.homeautomation.hvac;

import org.junit.Before;
import org.junit.Test;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.mock.MockTransceiver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/8/12
 * Time: 9:10 AM
 */
@SuppressWarnings("UnqualifiedInnerClassAccess")
public class HvacStateEngineThreadedTest {

    private HvacStateEngine stateEngine = null;
    private HvacSettings settings = null;
    private Sensors sensors = null;
    private HvacMechanical mechanical = null;

    private enum State {OFF, STAGE_1, STAGE_2}

    @Before
    public void setUp() {
        this.settings = new HvacSettings();
        this.mechanical = new HvacMechanical();
        this.mechanical.init();
        this.mechanical.setTransceiver(new MockTransceiver());
        this.mechanical.junitSetLoopDelay(1L);
        this.sensors = new Sensors();
        this.stateEngine = new HvacStateEngine(this.settings, this.mechanical, this.sensors);
        this.stateEngine.junitSetLoopDelay(1L);
    }

    @Test
    public void testStartStage1ThenChangeSetPointToStage2() throws InterruptedException {
        _testStartStage1ThenChangeSetPointToStage2(Mode.HEAT);
        _testStartStage1ThenChangeSetPointToStage2(Mode.COOL);
    }

    @Test
    public void testStartStage1ThenFailToSatisfyThermostat() throws InterruptedException {
        _testStartStage1ThenFailToSatisfyThermostat(Mode.HEAT);
        _testStartStage1ThenFailToSatisfyThermostat(Mode.COOL);
    }

    @Test
    public void testNoRecentThermostatData() throws InterruptedException {
        this._testNoRecentThermostatData(Mode.HEAT);
        this._testNoRecentThermostatData(Mode.COOL);
    }

    private void _testNoRecentThermostatData(final Mode mode) throws InterruptedException {
        // start stage 1
        this.settings.setMode(mode);
        final Temperature anyTemp = new CelsiusTemperature(25.0);
        this.sensors.setIndoorTemperature(anyTemp);
        this.settings.setHoldTemperature(getStage1HoldTemp(anyTemp, mode));
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.STAGE_1, getState());

        // wait for a while
        adjustTimeThenRunWorkerThread(10L * 60L * 1000L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        adjustTimeThenRunWorkerThread(10L * 60L * 1000L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.OFF, getState());

        // restart thermostat
        this.sensors.setIndoorTemperature(anyTemp);
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(mode, this.settings.getMode());
        assertEquals(State.STAGE_1, getState());
    }

    private void _testStartStage1ThenChangeSetPointToStage2(final Mode mode) throws InterruptedException {
        // start stage 1
        this.settings.setMode(mode);
        final Temperature anyTemp = new CelsiusTemperature(25.0);
        this.sensors.setIndoorTemperature(anyTemp);
        this.settings.setHoldTemperature(getStage1HoldTemp(anyTemp, mode));
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.STAGE_1, getState());

        // change to stage 2
        this.settings.setHoldTemperature(getStage2HoldTemp(anyTemp, mode));
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.STAGE_2, getState());

        // leave it on for a while to satisfy minimum time on/off
        Manager.junitAdjustDateNow(60L * 60L * 1000L);
        this.sensors.setIndoorTemperature(anyTemp.incrementInCelsius(0.1));
        this.runWorkerThread(this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.STAGE_2, getState());

        // satisfy thermostat
        this.sensors.setIndoorTemperature(getStage2SatisfyTemp(anyTemp, mode));
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.OFF, getState());
    }

    private void _testStartStage1ThenFailToSatisfyThermostat(final Mode mode) throws InterruptedException {
        // start stage 1
        this.settings.setMode(mode);
        final Temperature anyTemp = new CelsiusTemperature(25.0);
        this.sensors.setIndoorTemperature(anyTemp);
        this.settings.setHoldTemperature(getStage1HoldTemp(anyTemp, mode));
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.STAGE_1, getState());

        // wait for stage 2
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final long stage2time = Long.parseLong(
                properties.getSystemProperty(CombinedProperties.PROP_STAGE2_UPSHIFT_TIME));
        Manager.junitAdjustDateNow(stage2time + 1L);
        this.sensors.setIndoorTemperature(anyTemp.incrementInCelsius(0.1));  // so that there is a recent temp sensor reading
        runWorkerThread(this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.STAGE_2, getState());

        // leave it on for a while to satisfy minimum time on/off
        adjustTimeThenRunWorkerThread(60L * 60L * 1000L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);

        // satisfy thermostat
        this.sensors.setIndoorTemperature(getStage2SatisfyTemp(anyTemp, mode));
        adjustTimeThenRunWorkerThread(10L, this.stateEngine);
        adjustTimeThenRunWorkerThread(HvacMechanical.getMechanicalDelay(), this.mechanical);
        assertEquals(State.OFF, getState());    }

    private State getState() {
        if (this.stateEngine.isOn(Equipment.HEAT_2) || this.stateEngine.isOn(Equipment.COOL_2)) {
            return HvacStateEngineThreadedTest.State.STAGE_2;
        }
        if (this.stateEngine.isOn(Equipment.HEAT_1) || this.stateEngine.isOn(Equipment.COOL_1)) {
            return HvacStateEngineThreadedTest.State.STAGE_1;
        }
        return HvacStateEngineThreadedTest.State.OFF;
    }

//    private Temperature getStage1SatisfyTemp(final Temperature temp, final Mode mode) {
//        if (mode != Mode.HEAT && mode != Mode.COOL) {
//            fail();
//        }
//
//        final Mode oppositeMode = (mode == Mode.HEAT)? Mode.COOL : Mode.HEAT;
//        return getStage1HoldTemp(temp, oppositeMode);
//    }

    private Temperature getStage1HoldTemp(final Temperature temp, final Mode mode) {
        final double hysteresis = this.settings.getCelsiusHysteresis();

        if (mode == Mode.COOL) {
            return temp.getTempMinusCelsiusHysteresis(hysteresis).decrementInCelsius(0.1);
        }
        if (mode == Mode.HEAT) {
            return temp.getTempPlusCelsiusHysteresis(hysteresis).incrementInCelsius(0.1);
        }
        fail();
        return temp;  // suppress compiler warning
    }

    private Temperature getStage2SatisfyTemp(final Temperature temp, final Mode mode) {
        if (mode != Mode.HEAT && mode != Mode.COOL) {
            fail();
        }

//        final Mode oppositeMode = (mode == Mode.HEAT)? Mode.COOL : Mode.HEAT;
//        final Temperature incrTemp = (mode == Mode.HEAT)? temp.incrementInCelsius(1.0) : temp.decrementInCelsius(1.0);
//        return getStage2HoldTemp(incrTemp, oppositeMode);
        return getStage2HoldTemp(getStage2HoldTemp(temp, mode), mode);
    }

    private Temperature getStage2HoldTemp(final Temperature temp, final Mode mode) {
        final double stage2Threshold = this.settings.getCelsiusSecondStageThreshold();

        if (mode == Mode.COOL) {
            return temp.getTempMinusCelsiusHysteresis(stage2Threshold)
                    .decrementInCelsius(0.1);
        }
        if (mode == Mode.HEAT) {
            return temp.getTempPlusCelsiusHysteresis(stage2Threshold)
                    .incrementInCelsius(0.1);
        }
        fail();
        return temp;  // suppress compiler warning
    }

    private void runWorkerThread(final Worker worker) throws InterruptedException {
        worker.start();
        Thread.sleep(10L);
        worker.stop();
        Thread.sleep(10L);
    }

    private void adjustTimeThenRunWorkerThread(final long time, final Worker worker) throws InterruptedException {
        Manager.junitAdjustDateNow(time + 1L);
        runWorkerThread(worker);
    }


}
