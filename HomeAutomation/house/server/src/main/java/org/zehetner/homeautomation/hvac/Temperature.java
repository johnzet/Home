package org.zehetner.homeautomation.hvac;

public interface Temperature {

	void setCelsiusTemperature(double cTemp);

	double getCelsiusTemperature();

	double getFahrenheitTemperature();

    Temperature incrementInFahrenheit(double degreesF);

    Temperature incrementInCelsius(double degreesC);

    Temperature decrementInFahrenheit(double degreesF);

    Temperature decrementInCelsius(double degreesC);

    boolean isLessThan(Temperature otherTemp);

    boolean isGreaterThan(Temperature otherTemp);

    Temperature getTempPlusCelsiusHysteresis(double hysteresis);

    Temperature getTempMinusCelsiusHysteresis(double hysteresis);
}
