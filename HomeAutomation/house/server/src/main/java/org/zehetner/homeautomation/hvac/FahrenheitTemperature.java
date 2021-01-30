package org.zehetner.homeautomation.hvac;

public class FahrenheitTemperature extends AbstractTemperature {
	public FahrenheitTemperature(final double fahrenheitTemp) {
		setCelsiusTemperature(fToC(fahrenheitTemp));
	}

    private double fToC(final double fahrenheitTemp) {
        return (fahrenheitTemp - 32.0) * 5.0/9.0;
    }

   public String toString() {
       return "Fahrenheit Temperature = " + getFahrenheitTemperature();
   }
}
