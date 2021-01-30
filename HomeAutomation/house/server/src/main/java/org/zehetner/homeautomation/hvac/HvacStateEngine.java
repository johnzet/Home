package org.zehetner.homeautomation.hvac;


import org.apache.log4j.Logger;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;

public class HvacStateEngine implements Worker {
    private static final Logger LOG = Logger.getLogger(HvacStateEngine.class.getName());
    private static final long CALCULATION_INTERVAL = 1000L;
    private long junitLoopDelay = 0L;
    private Thread stateEngineThread = null;
    private final HvacMechanical hvacMechanical;
    private final HvacSettings hvacSettings;
    private Sensors sensors = null;

    public HvacStateEngine(final HvacSettings hvacSettingsArg, final HvacMechanical hvacMechanicalArg) {
        this.hvacSettings = hvacSettingsArg;
        this.hvacMechanical = hvacMechanicalArg;
    }

    public HvacStateEngine(final HvacSettings hvacSettingsArg, final HvacMechanical hvacMechanicalArg,
                           final Sensors sensorsArg) {
        this(hvacSettingsArg, hvacMechanicalArg);
        this.sensors = sensorsArg;
    }

    public void calculateState() {

        LOG.debug("Starting hvac calculateState()");

        this.hvacMechanical.setOnByProposal(Equipment.FAN, this.hvacSettings.isFanOn());

        if (!this.sensors.isIndoorTemperatureRecent()) {
            this.hvacMechanical.setOnByProposal(Equipment.COOL_1, false);
            this.hvacMechanical.setOnByProposal(Equipment.HEAT_1, false);
            this.hvacMechanical.setOnByProposal(Equipment.COOL_2, false);
            this.hvacMechanical.setOnByProposal(Equipment.HEAT_2, false);
            return;
        }

        final Temperature holdTemperature = this.hvacSettings.getHoldTemperature();
	    final Temperature currentTemperature = this.sensors.getIndoorTemperature();
        final double hysteresis = this.hvacSettings.getCelsiusHysteresis();
        final double stage2Threshold = this.hvacSettings.getCelsiusSecondStageThreshold();
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final long upshiftTime = Long.parseLong(
                properties.getSystemProperty(CombinedProperties.PROP_STAGE2_UPSHIFT_TIME));


        switch (this.hvacSettings.getMode()) {
	        case HEAT:
                this.hvacMechanical.setOnByProposal(Equipment.COOL_1, false);
                this.hvacMechanical.setOnByProposal(Equipment.COOL_2, false);
                if (currentTemperature.getTempPlusCelsiusHysteresis(hysteresis).isLessThan(holdTemperature)) {
                    this.hvacMechanical.setOnByProposal(Equipment.HEAT_1, true);
                    if (currentTemperature.getTempPlusCelsiusHysteresis(stage2Threshold).isLessThan(holdTemperature)) {
                        this.hvacMechanical.setOnByProposal(Equipment.HEAT_2, true);
                    }
                } else if (currentTemperature.getTempMinusCelsiusHysteresis(hysteresis).isGreaterThan(holdTemperature)) {
                    this.hvacMechanical.setOnByProposal(Equipment.HEAT_1, false);
                    this.hvacMechanical.setOnByProposal(Equipment.HEAT_2, false);
                }
                if (this.hvacMechanical.isOnByProposal(Equipment.HEAT_1) && this.hvacMechanical.getTimeOn(Equipment.HEAT_1) > upshiftTime) {
                    this.hvacMechanical.setOnByProposal(Equipment.HEAT_2, true);
                }
                break;
	        case COOL:
                this.hvacMechanical.setOnByProposal(Equipment.HEAT_1, false);
                this.hvacMechanical.setOnByProposal(Equipment.HEAT_2, false);
                if (currentTemperature.getTempMinusCelsiusHysteresis(hysteresis).isGreaterThan(holdTemperature)) {
                    this.hvacMechanical.setOnByProposal(Equipment.COOL_1, true);
                    if (currentTemperature.getTempMinusCelsiusHysteresis(stage2Threshold).isGreaterThan(holdTemperature)) {
                            this.hvacMechanical.setOnByProposal(Equipment.COOL_2, true);
                    }
                } else if (currentTemperature.getTempPlusCelsiusHysteresis(hysteresis).isLessThan(holdTemperature)) {
                    this.hvacMechanical.setOnByProposal(Equipment.COOL_1, false);
                    this.hvacMechanical.setOnByProposal(Equipment.COOL_2, false);
                }
                if (this.hvacMechanical.isOnByProposal(Equipment.COOL_1) && this.hvacMechanical.getTimeOn(Equipment.COOL_1) > upshiftTime) {
                    this.hvacMechanical.setOnByProposal(Equipment.COOL_2, true);
                }
                break;
            case OFF:
                this.hvacMechanical.setOnByProposal(Equipment.COOL_1, false);
                this.hvacMechanical.setOnByProposal(Equipment.HEAT_1, false);
                this.hvacMechanical.setOnByProposal(Equipment.COOL_2, false);
                this.hvacMechanical.setOnByProposal(Equipment.HEAT_2, false);
	    }
	}

    public HvacSettings getHvacSettings() {
        return this.hvacSettings;
    }

    public HvacMechanical getHvacMechanical() {
        return this.hvacMechanical;
    }

    @Override
    public void start() {
        if (this.sensors == null) {
            this.sensors = Manager.getSingleton().getSensors();
        }
        this.stateEngineThread = new Thread(new HvacStateEngine.Engine());
        this.stateEngineThread.setName(HvacStateEngine.class.getName());
        this.stateEngineThread.start();
        LOG.info("Started HVAC State Engine thread");
    }

    @Override
    public void stop() {
        this.hvacMechanical.stop();
        if (this.stateEngineThread != null) {
            this.stateEngineThread.interrupt();
        }
        LOG.info("Stopped HVAC State Engine thread");
    }

    @Override
    public void junitSetLoopDelay(final long delayMs) {
        this.junitLoopDelay = delayMs;
    }

    public boolean isOn(final Equipment equipment) {
        return this.hvacMechanical.isOn(equipment);
    }

    private class Engine implements Runnable {
        @Override
        public void run() {
            LOG.debug("At the top of the HvacStateEngine.Engine loop.");
            boolean keepLooping = true;
            do {
                try {
                    calculateState();
                    Thread.sleep((HvacStateEngine.this.junitLoopDelay > 0L)? HvacStateEngine.this.junitLoopDelay : CALCULATION_INTERVAL);
                } catch (InterruptedException e) {
                    LOG.info("HvacStateEngine thread normal exit");
                    keepLooping = false;
                } catch (Throwable t) {
                    LOG.warn("HvacStateEngine thread exception caught: ", t);
                }
            } while (keepLooping);
        }
    }
}
