package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.joda.time.DateTime;
import org.zehetner.homeautomation.Time;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/19/12
 * Time: 6:31 PM
 */
@XStreamAlias("SprinklerProgram")
public class SprinklerProgram extends Program {
    private SprinklerRepeatPolicy repeat = null;
    private ActivationTime startTime = null;
    private final List<SprinklerAction> actions = new ArrayList<SprinklerAction>(5);

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

    public void junitSetRecentCompletion(final Date recentActivation) {
        this.repeat.setRecentCompletion(recentActivation);
    }

    public List<SprinklerAction> getActions() {
        return this.actions;
    }

    public void addAction(final SprinklerAction action) {
        this.actions.add(action);
    }

    public Zone getActiveZone() {
        if (!this.repeat.isOnToday()) {
            return Zone.ALL_OFF;
        }
        final int minuteOfDay = new DateTime(Manager.getDateNow()).getMinuteOfDay();
        int minutesIntoProgram = minuteOfDay - new Time(this.getStartTime().getSeconds()).getMinutesSinceMidnight();
        if (minutesIntoProgram < 0) {
            return Zone.ALL_OFF;
        }
        for (final SprinklerAction action : this.getActions()) {
            if (minutesIntoProgram < action.getDurationMinutes()) {
                this.repeat.setRecentCompletion(new Date(0L));
                return action.getZone();
            }
            minutesIntoProgram -= action.getDurationMinutes();
        }
        return Zone.ALL_OFF;
    }

    public void setRecentActivity() {
        this.repeat.setRecentCompletion(Manager.getDateNow());
    }

    public static class ActivationTime {
        private final int hour;
        private final int minute;

        public ActivationTime(final int hour, final int minute) {
            this.hour = hour;
            this.minute = minute;
        }

        public int getHour() {
            return this.hour;
        }

        public int getMinute() {
            return this.minute;
        }

        public int getSeconds() {
            return this.hour * 60 * 60 + this.minute * 60;
        }
    }
}
