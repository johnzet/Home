package org.zehetner.homeautomation.hvac;

import org.zehetner.homeautomation.common.CombinedProperties;
import org.zehetner.homeautomation.common.Manager;


public class HvacSettings {
//    private static final Logger LOG = Logger.getLogger(HvacSettings.class.getName());
    private Mode mode;
    private Temperature holdTemperature;
    private boolean fanOn;

    public HvacSettings() {
        this.mode = Mode.OFF;
        this.holdTemperature = new FahrenheitTemperature(70.0);
        this.fanOn = false;
    }

    public Temperature getHoldTemperature() {
        return this.holdTemperature;
    }

    public void setHoldTemperature(final Temperature temperature) {
        this.holdTemperature = temperature;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(final Mode modeArg) {
        this.mode = modeArg;
    }

    public boolean isFanOn() {
        return this.fanOn;
    }

    public void setFanOn(final boolean fanOnArg) {
        this.fanOn = fanOnArg;
    }

    public double getCelsiusHysteresis() {
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final Double heatHysteresis = Double.parseDouble(
                properties.getSystemProperty(CombinedProperties.PROP_HEAT_HYSTERESIS));
        final Double coolHysteresis = Double.parseDouble(
                properties.getSystemProperty(CombinedProperties.PROP_COOL_HYSTERESIS));

        if (this.mode == Mode.COOL) {
            return coolHysteresis;
        }
        if (this.mode == Mode.HEAT) {
            return heatHysteresis;
        }
        return 0.2;  //should be meaningless, but not null
    }

    public double getCelsiusSecondStageThreshold() {
        final CombinedProperties properties = Manager.getSingleton().getProperties();
        final Double heat2Threshold = Double.parseDouble(
                properties.getSystemProperty(CombinedProperties.PROP_HEAT_2_THRESHOLD));
        final Double cool2Threshold = Double.parseDouble(
                properties.getSystemProperty(CombinedProperties.PROP_COOL_2_THRESHOLD));

        if (this.mode == Mode.COOL) {
            return cool2Threshold;
        }
        if (this.mode == Mode.HEAT) {
            return heat2Threshold;
        }
        return 0.2;  //should be meaningless, but not null
    }

    @Override
    public String toString() {
        return "HvacSettings{" +
                "mode=" + this.mode +
                ", holdTemp=" + this.holdTemperature +
                '}';
    }
}
