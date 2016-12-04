package org.zehetner.homeautomation;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/23/12
 * Time: 5:05 PM
 */
public class Time {
    private final int seconds;

    public Time(final int hours, final int minutes, final int seconds) {
        this.seconds = seconds + minutes * 60 + hours * 60 * 60;
    }

    public Time(final int secondsArg) {
        this.seconds = secondsArg;
    }

    public int getSecondsSinceMidnight() {
        return this.seconds;
    }

    public int getHours() {
        return (int)Math.floor(this.seconds / 3600);
    }

    public int getMinutes() {
        return (this.seconds - getSeconds() - (getHours()*3600)) / 60;
    }

    public int getSeconds() {
        return this.seconds % 60;
    }

    public int getMinutesSinceMidnight() {
        return (this.seconds - getSeconds() ) / 60;
    }

    @Override
    public boolean equals(final Object obj) {
        if (! (obj instanceof Time)) {
            return false;
        }
        return this.getSecondsSinceMidnight() == ((Time)obj).getSecondsSinceMidnight();
    }
}


