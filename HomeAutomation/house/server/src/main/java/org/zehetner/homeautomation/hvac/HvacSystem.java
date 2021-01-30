package org.zehetner.homeautomation.hvac;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/28/12
 * Time: 7:47 PM
 */
public class HvacSystem {
    private final HvacStateEngine hvacStateEngine;

    public HvacSystem() {
        final HvacSettings hvacSettings = new HvacSettings();
        final HvacMechanical hvacMechanical = new HvacMechanical();
        this.hvacStateEngine = new HvacStateEngine(hvacSettings, hvacMechanical);
    }

    public HvacStateEngine getHvacStateEngine() {
        return this.hvacStateEngine;
    }

    public void start() {
        this.hvacStateEngine.getHvacMechanical().start();
        this.hvacStateEngine.start();
    }

    public void stop() {
        this.hvacStateEngine.getHvacMechanical().stop();
        this.hvacStateEngine.stop();
    }
}
