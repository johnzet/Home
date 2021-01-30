package org.zehetner.homeautomation.hvac;

import org.junit.Test;
import org.zehetner.homeautomation.utils.TestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TemperatureTest {

	@Test
	public void testFtoC() {
		final double anyTemp = 42.0;
		final Temperature t = new FahrenheitTemperature(anyTemp);
		assertTrue(TestUtils.closeEnough(t.getCelsiusTemperature(), (anyTemp - 32.0) * 5.0 / 9.0));
	}

	@Test
	public void testCtoF() {
		final double anyTemp = 42.0;
		final Temperature t = new CelsiusTemperature(anyTemp);
		assertTrue(TestUtils.closeEnough(t.getFahrenheitTemperature(), (anyTemp * 9.0 / 5.0) + 32.0));
	}

	@Test
	public void testIncrementInFahrenheit() {
	    final Temperature a = new FahrenheitTemperature(42.0);
        assertTrue(TestUtils.closeEnough(39.5, a.incrementInFahrenheit(-2.5).getFahrenheitTemperature()));
	}

    @Test
    public void testIncrementInCelsius() {
        final Temperature a = new CelsiusTemperature(42.0);
        assertTrue(TestUtils.closeEnough(39.5, a.incrementInCelsius(-2.5).getCelsiusTemperature()));
    }

    @Test
	public void testDecrementInFahrenheit() {
        final Temperature a = new FahrenheitTemperature(42.0);
        assertTrue(TestUtils.closeEnough(40.5, a.decrementInFahrenheit(1.5).getFahrenheitTemperature()));
    }

    @Test
    public void testDecrementInCelsius() {
        final Temperature a = new CelsiusTemperature(42.0);
        assertTrue(TestUtils.closeEnough(40.5, a.decrementInCelsius(1.5).getCelsiusTemperature()));
    }

    @Test
	public void testLessThan() {
        final Temperature a = new CelsiusTemperature(7.0);
        final Temperature b = new CelsiusTemperature(8.0);
        assertTrue(a.isLessThan(b));

        final Temperature x = new CelsiusTemperature(7.0001);
        final Temperature y = new CelsiusTemperature(7.0);
        assertFalse(x.isLessThan(y));
	}

	@Test
	public void testGreaterThan() {
        final Temperature a = new CelsiusTemperature(8.0);
        final Temperature b = new CelsiusTemperature(7.0);
        assertTrue(a.isGreaterThan(b));

        final Temperature x = new CelsiusTemperature(7.0);
        final Temperature y = new CelsiusTemperature(7.0001);
        assertFalse(x.isGreaterThan(y));
	}

    @Test
    public void testToString() {
        final Temperature a = new CelsiusTemperature(20.0);
        assertTrue(a.toString().contains("20.0"));

        final Temperature b = new FahrenheitTemperature(70.0);
        assertTrue(b.toString().contains("70.0"));
    }
}
