package org.zehetner.homeautomation.common;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/14/2016
 * Time: 3:52 PM
 */
public class TempHumSensorSample implements SensorSample {
    private Measurement temperature;
    private Measurement humidity;

    public TempHumSensorSample(Measurement temp, Measurement hum) {
        this.temperature = temp;
        this.humidity = hum;
    }

    public Measurement getHumidity() {
        return humidity;
    }

    public Measurement getTemperature() {
        return temperature;
    }
}
