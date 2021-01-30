package org.zehetner.homeautomation.hvac;

import org.junit.After;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.utils.TestUtils;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SensorsTest {

    @After
    public void tearDown() {
        Manager.junitSetDateNow(null);
    }

	@Test
	public void testIndoorHumidity() {
		final Sensors sensors = new Sensors();
        sensors.setIndoorHumidity(42.0);
		assertTrue(TestUtils.closeEnough(sensors.getIndoorHumidity(), 42.0));
	}

	@Test
	public void testIndoorTemp() {
        final Sensors sensors = new Sensors();
        sensors.setIndoorTemperature(new FahrenheitTemperature(42.0));
		assertTrue(TestUtils.closeEnough(sensors.getIndoorTemperature().getFahrenheitTemperature(), 42.0));
	}

    @Test
    public void testOutdoorTemp() {
        final Sensors sensors = new Sensors();
        sensors.setOutdoorTemperature(new CelsiusTemperature(42.0));
        assertTrue(TestUtils.closeEnough(sensors.getOutdoorTemperature().getCelsiusTemperature(), 42.0));
    }

    @Test
    public void testToString() {
        final String str = new Sensors().toString();
        assertTrue(str.contains("inTemp"));
        assertTrue(str.contains("inHum"));
        assertTrue(str.contains("outTemp"));
    }

    @Test
    public void testIndoorTempIsRecent() {
        final Sensors sensors = new Sensors();
        sensors.setIndoorTemperature(new CelsiusTemperature(42.0));
        assertTrue(sensors.isIndoorTemperatureRecent());

        Manager.junitSetDateNow(new Date(System.currentTimeMillis() +(30L * 1000L)));
        assertTrue(sensors.isIndoorTemperatureRecent());
    }

    @Test
    public void testIndoorTempIsOld() {
        final Sensors sensors = new Sensors();
        sensors.setIndoorTemperature(new CelsiusTemperature(42.0));
        Manager.junitSetDateNow(new Date(System.currentTimeMillis() + (6L * 60L * 1000L)));
        assertFalse(sensors.isIndoorTemperatureRecent());

    }
}
