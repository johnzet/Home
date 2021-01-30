package org.zehetner.homeautomation.hvac;

/**
* Created by IntelliJ IDEA.
* User: johnz
* Date: 12/20/11
* Time: 11:46 PM
* To change this template use File | HvacSettings | File Templates.
*/
public enum Mode {
    OFF, HEAT, COOL;

    public static String toString(final Mode mode) {
        if (mode == Mode.HEAT) {
            return "Heat";
        } else if (mode == Mode.COOL) {
            return "A/C";
        } else {
            return "Off";
        }
    }
}
