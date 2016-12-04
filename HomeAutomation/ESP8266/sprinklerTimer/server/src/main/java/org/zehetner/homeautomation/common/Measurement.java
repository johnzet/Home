package org.zehetner.homeautomation.common;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/14/2016
 * Time: 3:49 PM
 */
public class Measurement {
    private double value;
    private Unit unit;

    public Measurement(double v, Unit u) {
        this.value = v;
        this.unit = u;
    }

    public Unit getUnit() {
        return unit;
    }

    public double getValue() {
        return value;
    }
}
