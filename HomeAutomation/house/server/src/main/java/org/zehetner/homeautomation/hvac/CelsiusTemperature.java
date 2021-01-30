package org.zehetner.homeautomation.hvac;

public class CelsiusTemperature extends AbstractTemperature {

	public CelsiusTemperature(final double celsiusTemperature) {
		setCelsiusTemperature(celsiusTemperature);
	}

    public String toString() {
        return "Celsius Temperature = " + getCelsiusTemperature();
    }
}
