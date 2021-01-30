package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.zehetner.homeautomation.common.Manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/23/12
 * Time: 5:02 PM
 */
@XStreamAlias("SprinklerRepeatPolicy")
public class SprinklerRepeatPolicy {
    private final SprinklerRepeatPolicy.Type type;
    private int optionalDaysInterval = 1;
    private final List<DayOfWeek> dayList = new ArrayList<DayOfWeek>(3);

    @XStreamOmitField
    private DateTime recentCompletion = new DateTime().minusYears(1);

    public SprinklerRepeatPolicy(final SprinklerRepeatPolicy.Type typeArg, final List<DayOfWeek> dayList) {
        this.type = typeArg;
        this.dayList.clear();
        if (dayList != null) {
            this.dayList.addAll(dayList);
        }
    }

    public void setRecentCompletion(final DateTime recentCompletionArg) {
        this.recentCompletion = new DateTime(recentCompletionArg);
    }

    boolean isOnToday() {
        final DateTime now = Manager.getDateNow();
        final int dayOfTheMonth = now.getDayOfMonth();

        if (SprinklerRepeatPolicy.Type.ON_DEMAND == this.type) {
            return false;
        }
        if (this.recentCompletion == null) this.recentCompletion = new DateTime().minusYears(1);
        final Duration timeOff = new Duration(this.recentCompletion.toInstant(), Manager.getDateNow().toInstant());
        if (SprinklerRepeatPolicy.Type.EVEN_DAYS == this.type) {
            return (dayOfTheMonth%2 == 0);
        } else if (SprinklerRepeatPolicy.Type.ODD_DAYS == this.type) {
            return (dayOfTheMonth%2 == 1);
        } else if (SprinklerRepeatPolicy.Type.EVERY_N_DAYS == this.type) {
            return timeOff.getStandardDays() >= optionalDaysInterval;
        } else if (SprinklerRepeatPolicy.Type.SELECT_DAYS == this.type) {
            for (DayOfWeek dow : this.dayList) {
                if (dow.isTheSameDay(now)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected Type getType() {
        return this.type;
    }

    protected List<DayOfWeek> getDayList() {
        return this.dayList;
    }

    public int getOptionalDaysInterval() {
        return optionalDaysInterval;
    }

    public void setOptionalDaysInterval(final int optionalDaysIntervalArg) {
        this.optionalDaysInterval = optionalDaysIntervalArg;
    }

    public enum Type {
        ON_DEMAND,
        EVEN_DAYS,
        ODD_DAYS,
        EVERY_N_DAYS,
        SELECT_DAYS
    }

    @XStreamAlias("DayOfWeek")
    protected enum DayOfWeek {
        SUNDAY (Calendar.SUNDAY),
        MONDAY (Calendar.MONDAY),
        TUESDAY (Calendar.TUESDAY),
        WEDNESDAY (Calendar.WEDNESDAY),
        THURSDAY (Calendar.THURSDAY),
        FRIDAY (Calendar.FRIDAY),
        SATURDAY (Calendar.SATURDAY);

        private final int calendarDayOfWeek;

        /**
         *
         * @param calendarDayOfWeek the day number used in the Calendar class
         */
        DayOfWeek(final int calendarDayOfWeek) {
            this.calendarDayOfWeek = calendarDayOfWeek;
        }

        public boolean isTheSameDay(DateTime dateTime) {
            // DateTime defines Monday as 1 and Sunday as 7
            int dateTimeDayOfWeek = dateTime.getDayOfWeek();
            if (dateTimeDayOfWeek == 6) {
                dateTimeDayOfWeek = 7;
            } else if (dateTimeDayOfWeek == 7) {
                dateTimeDayOfWeek = 1;
            }  else {
                dateTimeDayOfWeek += 1;
            }
            return this.calendarDayOfWeek == dateTimeDayOfWeek;
        }
    }
}
