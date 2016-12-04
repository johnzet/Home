package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/19/12
 * Time: 6:31 PM
 */
@XStreamAlias("SprinklerProgram")
public class SprinklerProgram extends Program {
    private static final Logger LOG = Logger.getLogger(SprinklerProgram.class.getName());

    private String name = null;
    private boolean enabled = true;
    private int multiplier = 100;
    private SprinklerRepeatPolicy repeat = null;
    private ActivationTime startTime = null;
    private final List<SprinklerAction> actions = new ArrayList<SprinklerAction>(5);

    @XStreamOmitField
    private ActivationTime onDemandStartTime = null;

    public ActivationTime getOnDemandStartTime() {
        return onDemandStartTime;
    }

    public void setOnDemandStartTime(final ActivationTime onDemandStartTimeArg) {
        this.onDemandStartTime = onDemandStartTimeArg;
    }

    public void setOnDemandStartTime(final DateTime dateNow) {
        ActivationTime st = new ActivationTime(dateNow.getHourOfDay(), dateNow.getMinuteOfHour());
        setOnDemandStartTime(st);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String nameArg) {
        if (this.name == null) this.name = "Default name";
        this.name = nameArg;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabledArg) {
        this.enabled = enabledArg;
        if (!enabledArg) this.setOnDemandStartTime((ActivationTime)null);
    }

    public int getMultiplier() {
        multiplier = Integer.max(multiplier, 10);
        multiplier = Integer.min(multiplier, 500);
        return multiplier;
    }

    public void setMultiplier(final int multiplierArg) {
        this.multiplier = multiplierArg;
    }

    public SprinklerRepeatPolicy getRepeat() {
        return this.repeat;
    }

    public void setRepeat(final SprinklerRepeatPolicy repeatArg) {
        this.repeat = repeatArg;
    }

    public ActivationTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(final ActivationTime startTimeArg) {
        this.startTime = startTimeArg;
    }

    public void junitSetRecentCompletion(final DateTime recentActivationArg) {
        this.repeat.setRecentCompletion(recentActivationArg);
    }

    public List<SprinklerAction> getActions() {
        return this.actions;
    }

    public void addAction(final SprinklerAction action) {
        this.actions.add(action);
    }

    public Zone getActiveZone() {
        if (this.getRepeat() == null) return Zone.ALL_OFF;

        Zone activeZone = Zone.ALL_OFF;
        if (this.getOnDemandStartTime() == null && (!this.repeat.isOnToday() || !this.isEnabled())) {
            activeZone = Zone.ALL_OFF;
        } else {
            ActivationTime st = this.getOnDemandStartTime();
            if (st != null) {
                activeZone = getActiveZoneByTime(st);
            }
            if (activeZone == null || activeZone == Zone.ALL_OFF) {
                activeZone = getActiveZoneByTime(this.getStartTime());
            }
        }
        LOG.debug("SprinklerProgram.getActiveZone() returning zone " + activeZone);
        return activeZone;
    }

    private Zone getActiveZoneByTime(ActivationTime startTimeArg) {
        final int minuteOfDay = Manager.getDateNow().getMinuteOfDay();
        int minutesIntoProgram = minuteOfDay - startTimeArg.getMinutesSinceMidnight();
        minutesIntoProgram /= (this.getMultiplier()/100.0);

        Zone activeZone = Zone.ALL_OFF;
        if (minutesIntoProgram < 0) {
            activeZone = Zone.ALL_OFF;
        } else {
            for (final SprinklerAction action : this.getActions()) {
                if (minutesIntoProgram < action.getDurationMinutes()) {
                    activeZone = action.getZone();
                    break;
                } else {
                    minutesIntoProgram -= action.getDurationMinutes();
                }
            }
            if (minutesIntoProgram > getProgramLength()) {
                setOnDemandStartTime((ActivationTime)null);
            }
        }
        return activeZone;
    }

    public void setRecentActivity() {
        if (this.getRepeat() != null) this.repeat.setRecentCompletion(Manager.getDateNow());
    }

    private int getProgramLength() {
        int length = 0;
        for (final SprinklerAction action : this.getActions()) {

            length += action.getDurationMinutes() * 100.0 / this.getMultiplier();
        }
        return length;
    }

    public static class ActivationTime {
        private final int hour;
        private final int minute;

        public ActivationTime(final int hourArg, final int minuteArg) {
            this.hour = hourArg;
            this.minute = minuteArg;
        }

        public int getHour() {
            return this.hour;
        }

        public int getMinute() {
            return this.minute;
        }

        public int getMinutesSinceMidnight() {
                    return this.minute + 60*this.hour;
                }
    }
}
