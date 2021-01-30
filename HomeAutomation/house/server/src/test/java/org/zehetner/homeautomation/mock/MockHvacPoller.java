package org.zehetner.homeautomation.mock;

import org.zehetner.homeautomation.hvac.CelsiusTemperature;
import org.zehetner.homeautomation.hvac.Sensors;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 5:31 PM
 * To change this template use File | HvacSettings | File Templates.
 */
public class MockHvacPoller implements Runnable {
    private Sensors sensors;

    public MockHvacPoller(final Sensors sensorsArg) {
        this.sensors = sensorsArg;  // Different classloader means different singleton, so pass it in.
    }

    @Override
    public void run() {
        sensors.setIndoorTemperature(new CelsiusTemperature(7));
        sensors.setIndoorHumidity(42.0);
    }
}

