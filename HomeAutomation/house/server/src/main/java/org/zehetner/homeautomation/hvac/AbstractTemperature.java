package org.zehetner.homeautomation.hvac;

public abstract class AbstractTemperature implements Temperature{
    private double celsiusTemperature = Double.NaN;

    protected AbstractTemperature() {
    }

	@Override
    public final void setCelsiusTemperature(final double cTemp) {
		this.celsiusTemperature = cTemp;
	}

	@Override
    public final double getCelsiusTemperature() {
		return this.celsiusTemperature;
	}

	@Override
    public final double getFahrenheitTemperature() {
		return AbstractTemperature.cToF(this.celsiusTemperature);
	}

	protected static double cToF(final double celsiusTemp) {
		return (celsiusTemp * (9.0/5.0) ) + 32.0;
	}

    @Override
    public final Temperature incrementInFahrenheit(final double degreesF) {
        return new FahrenheitTemperature(this.getFahrenheitTemperature() + degreesF);
    }

    @Override
    public final Temperature incrementInCelsius(final double degreesC) {
        return new CelsiusTemperature(this.getCelsiusTemperature() + degreesC);
    }

    @Override
    public final Temperature decrementInFahrenheit(final double degreesF) {
        return new FahrenheitTemperature(this.getFahrenheitTemperature() - degreesF);
    }

    @Override
    public final Temperature decrementInCelsius(final double degreesC) {
        return new CelsiusTemperature(this.getCelsiusTemperature() - degreesC);
    }

    @Override
    public final boolean isLessThan(final Temperature otherTemp) {
        return this.getCelsiusTemperature() < otherTemp.getCelsiusTemperature();
    }

    @Override
    public final boolean isGreaterThan(final Temperature otherTemp) {
        return this.getCelsiusTemperature() > otherTemp.getCelsiusTemperature();
    }

    @Override
    public Temperature getTempPlusCelsiusHysteresis(final double hysteresis) {
        return new CelsiusTemperature(this.celsiusTemperature + hysteresis);
    }

    @Override
    public Temperature getTempMinusCelsiusHysteresis(final double hysteresis) {
        return new CelsiusTemperature(this.celsiusTemperature - hysteresis);
    }

    @Override
    public String toString() {
        return String.format("%.2f deg C   %.2f deg F", this.celsiusTemperature, getFahrenheitTemperature());
    }

}
