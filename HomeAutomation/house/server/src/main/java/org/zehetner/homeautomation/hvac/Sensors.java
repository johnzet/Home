package org.zehetner.homeautomation.hvac;

import org.zehetner.homeautomation.common.Manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Sensors {

	private Temperature outdoorTemperature;
	private Temperature indoorTemperature;
    private double indoorHumidity;
    private double outdoorHumidity;
    private static final int BAROMETER_AVERAGING_SAMPLE_COUNT = 20;
    private final List<Double> recentBarometerSamples = new ArrayList<Double>(BAROMETER_AVERAGING_SAMPLE_COUNT);
    private static final int BATTERY_AVERAGING_SAMPLE_COUNT = 20;
    private final List<Double> recentBatterySamples = new ArrayList<Double>(BATTERY_AVERAGING_SAMPLE_COUNT);

    private Date lastIndoorTempSetTime = new Date(0L);

    public double getOutdoorHumidity() {
        return this.outdoorHumidity;
    }

    public void setOutdoorHumidity(final double outdoorHumidityArg) {
        this.outdoorHumidity = outdoorHumidityArg;
    }

    public double getBarometer() {
        double sum = 0.0;
        for (final Double d : this.recentBarometerSamples) {
            sum += d;
        }
        return sum / (double)this.recentBarometerSamples.size();
    }

    public void setBarometer(final double barometer) {
        this.recentBarometerSamples.add(0, barometer);
        if (this.recentBarometerSamples.size() > BAROMETER_AVERAGING_SAMPLE_COUNT) {
            this.recentBarometerSamples.remove(BAROMETER_AVERAGING_SAMPLE_COUNT);
        }
    }

    public double getThermostat1BatPercent() {
        double sum = 0.0;
        for (final Double d : this.recentBatterySamples) {
            sum += d;
        }
        return sum / (double)this.recentBatterySamples.size();
    }

    public void setThermostat1BatPercent(final double batPercent) {
        this.recentBatterySamples.add(0, batPercent);
        if (this.recentBatterySamples.size() > BATTERY_AVERAGING_SAMPLE_COUNT) {
            this.recentBatterySamples.remove(BATTERY_AVERAGING_SAMPLE_COUNT);
        }
    }

    public Sensors() {
        this.outdoorTemperature = new CelsiusTemperature(Double.NaN);

        this.indoorTemperature = new CelsiusTemperature(Double.NaN);
        this.indoorHumidity = Double.NaN;

        this.outdoorHumidity = Double.NaN;
	}

	public Temperature getOutdoorTemperature() {
		return this.outdoorTemperature;
	}

	public void setOutdoorTemperature(final Temperature outdoorTemperatureArg) {
		this.outdoorTemperature = outdoorTemperatureArg;
	}

	public Temperature getIndoorTemperature() {
		return this.indoorTemperature;
	}

    public boolean isIndoorTemperatureRecent() {
        return (Manager.getDateNow().getTime() - this.lastIndoorTempSetTime.getTime() < (5L * 60L * 1000L));
    }

	public void setIndoorTemperature(final Temperature indoorTemperatureArg) {
		this.indoorTemperature = indoorTemperatureArg;
        this.lastIndoorTempSetTime = Manager.getDateNow();
	}

    public void setIndoorHumidity(final double indoorHumidityArg) {
        this.indoorHumidity = indoorHumidityArg;
    }

    public double getIndoorHumidity() {
        return this.indoorHumidity;
    }


    @Override
    public String toString() {
        return "Sensors{" +
                "outTemp=" + this.outdoorTemperature +
                ", inTemp=" + this.indoorTemperature +
                ", inHum=" + this.indoorHumidity +
                '}';
    }

    public static double kPaToinHg(final double barom) {
        return barom * (1.0 / 0.827) * 0.296;  // "0.296" converts between units, "0.82*" adjusts for elevation
    }
}
