package org.zehetner.homeautomation.stateengine;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.zehetner.homeautomation.common.Manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/23/12
 * Time: 5:02 PM
 */
public class SprinklerRepeatPolicy {
    private final SprinklerRepeatPolicy.Type type;
    private final List<DayOfWeek> dayList = new ArrayList<DayOfWeek>(3);
    
//    @XStreamOmitField
    private Date recentCompletion = new Date(0L);

    public SprinklerRepeatPolicy(final SprinklerRepeatPolicy.Type typeArg, final List<DayOfWeek> dayList) {
        this.type = typeArg;
        this.dayList.clear();
        if (dayList != null) {
            this.dayList.addAll(dayList);
        }
    }

    public Date getRecentCompletion() {
        return (Date) this.recentCompletion.clone();
    }

    public void setRecentCompletion(final Date recentCompletion) {
        this.recentCompletion = (Date)recentCompletion.clone();
    }

    boolean isOnToday() {
        if (SprinklerRepeatPolicy.Type.ON_DEMAND == this.type) {
            return false;
        }
        final long daysOff = (long)Math.floor((Manager.getDateNow().getTime() - this.recentCompletion.getTime()) / (1000.0 * 3600.0 * 24.0));
        if (SprinklerRepeatPolicy.Type.DAILY == this.type) {
                return daysOff >= 1;
        } else if (SprinklerRepeatPolicy.Type.EVERY_TWO_DAYS == this.type) {
            return daysOff >= 2;
        } else if (SprinklerRepeatPolicy.Type.EVERY_THREE_DAYS == this.type) {
            return daysOff >= 3;
        } else if (SprinklerRepeatPolicy.Type.EVERY_FOUR_DAYS == this.type) {
            return daysOff >= 4;
        } else if (SprinklerRepeatPolicy.Type.SELECT_DAYS == this.type) {
            final Calendar calendar  = Calendar.getInstance();
            calendar.setTime(Manager.getDateNow());
            final DayOfWeek dayOfWeek = DayOfWeek.getByCalendarDayOfWeek(calendar.get(Calendar.DAY_OF_WEEK));
            for (DayOfWeek dow : this.dayList) {
                if (dow.getCalendarDayOfWeek() == dayOfWeek.getCalendarDayOfWeek()) {
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

    protected enum Type {
        ON_DEMAND,
        DAILY,
        EVERY_TWO_DAYS,
        EVERY_THREE_DAYS,
        EVERY_FOUR_DAYS,
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

        public int getCalendarDayOfWeek() {
            return this.calendarDayOfWeek;
        }

        public static DayOfWeek getByCalendarDayOfWeek(final int calendarDayOfWeek) {
            for (final DayOfWeek dow : DayOfWeek.values()) {
                if (dow.getCalendarDayOfWeek() == calendarDayOfWeek) {
                    return dow;
                }
            }
            throw new IllegalArgumentException("Unknown day of week: " + calendarDayOfWeek);
        }
    }
}
