package org.zehetner.homeautomation.hvac;

import junit.framework.TestCase;
import org.junit.Test;
import org.zehetner.homeautomation.utils.TestUtils;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/19/11
 * Time: 6:39 PM
 */
public class HvacSettingsTest extends TestCase {

    @Test
    public void testDefaultMode() {
        assertEquals(Mode.OFF, new HvacSettings().getMode());
    }

    @Test
    public void testDefaultHoldTemperature() {
        assertTrue(TestUtils.closeEnough(new HvacSettings().getHoldTemperature().getFahrenheitTemperature(), 70.0));
    }

    @Test
    public void testSetPoint() {
        final HvacSettings hvacSettings = new HvacSettings();
        hvacSettings.setHoldTemperature(new FahrenheitTemperature(80.0));
        assertTrue(TestUtils.closeEnough(hvacSettings.getHoldTemperature().getFahrenheitTemperature(), 80.0));
    }

    @Test
    public void testCoolHysteresis() {
        final HvacSettings hvacSettings = new HvacSettings();
        hvacSettings.setMode(Mode.COOL);
        assertTrue(hvacSettings.getCelsiusHysteresis() < 5.0);
        assertTrue(hvacSettings.getCelsiusHysteresis() > 0.1);
    }

    @Test
    public void testHeatHysteresis() {
        final HvacSettings hvacSettings = new HvacSettings();
        hvacSettings.setMode(Mode.HEAT);
        assertTrue(hvacSettings.getCelsiusHysteresis() < 5.0);
        assertTrue(hvacSettings.getCelsiusHysteresis() > 0.1);
    }

    @Test
    public void testToString() {
        String str = new HvacSettings().toString();
    }
}
