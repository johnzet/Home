package org.zehetner.homeautomation.hvac;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/20/12
 * Time: 10:36 AM
 */
public enum Equipment {

    // Note:  The physical relay number is the ordinal of this enum.

    ALL_OFF("All Off"),
    HEAT_1("Heat 1"),           // White
    HEAT_2("Heat 2"),           // Blue (varies)
    COOL_1("Cool 1"),           // Yellow
    COOL_2("Cool 2"),           // None - single-stage A/C
    FAN("Fan"),                 // Green
    HUMIDIFIER("Humidifier");   // None - future
                                // Red is the 24VAC power
                                //   line to be switched
                                //   to the others.

    private final String name;

    Equipment(final String nameArg) {
        this.name = nameArg;
    }

    public String getName() {
        return this.name;
    }

    public int getPhysicalRelayNumber() {
        return this.ordinal();
    }
}
