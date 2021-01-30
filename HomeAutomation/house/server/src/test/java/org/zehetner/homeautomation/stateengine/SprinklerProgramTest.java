package org.zehetner.homeautomation.stateengine;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/28/12
 * Time: 2:34 PM
 */
public class SprinklerProgramTest {

    private final String every3daysStr = "<list>\n" +
            "  <SprinklerProgram>\n" +
            "    <name>Program A</name>\n" +
            "    <repeat>\n" +
            "      <type>EVERY_THREE_DAYS</type>\n" +
            "      <dayList/>\n" +
            "      <recentCompletion>1970-01-01 00:00:00.0 UTC</recentCompletion>\n" +
            "    </repeat>\n" +
            "    <startTime>\n" +
            "      <hour>4</hour>\n" +
            "      <minute>30</minute>\n" +
            "    </startTime>\n" +
            "    <actions>\n" +
            "      <SprinklerAction>\n" +
            "        <durationMinutes>10</durationMinutes>\n" +
            "        <zone>ZONE_2</zone>\n" +
            "      </SprinklerAction>\n" +
            "      <SprinklerAction>\n" +
            "        <durationMinutes>15</durationMinutes>\n" +
            "        <zone>ZONE_3</zone>\n" +
            "      </SprinklerAction>\n" +
            "    </actions>\n" +
            "  </SprinklerProgram>\n" +
            "</list>\n";

    private final String selectDaysStr = "<list>\n" +
            "  <SprinklerProgram>\n" +
            "    <name>Program A</name>\n" +
            "    <repeat>\n" +
            "      <type>SELECT_DAYS</type>\n" +
            "      <dayList>\n" +
            "        <DayOfWeek>SUNDAY</DayOfWeek>\n" +
            "        <DayOfWeek>THURSDAY</DayOfWeek>\n" +
            "      </dayList>\n" +
            "      <recentCompletion>1970-01-01 00:00:00.0 UTC</recentCompletion>\n" +
            "    </repeat>\n" +
            "    <startTime>\n" +
            "      <hour>15</hour>\n" +
            "      <minute>20</minute>\n" +
            "    </startTime>\n" +
            "    <actions>\n" +
            "      <SprinklerAction>\n" +
            "        <durationMinutes>10</durationMinutes>\n" +
            "        <zone>ZONE_2</zone>\n" +
            "      </SprinklerAction>\n" +
            "      <SprinklerAction>\n" +
            "        <durationMinutes>15</durationMinutes>\n" +
            "        <zone>ZONE_3</zone>\n" +
            "      </SprinklerAction>\n" +
            "    </actions>\n" +
            "  </SprinklerProgram>\n" +
            "</list>\n";

    private SprinklerProgram parseProgram(final String progStr) {
        final ProgramSet progSet = new ProgramSet();
        progSet.loadFromXml(progStr);
        return (SprinklerProgram)progSet.getPrograms().get(0);
    }

    @Test
    public void testWrongDayRightTimeSelectDays() {
        final SprinklerProgram prog = parseProgram(this.selectDaysStr);
        final Date date = getDay(new Date(2013/*year*/, 7/*month*/, 15/*date*/, 15/*hrs*/, 31/*min*/), DateTimeConstants.TUESDAY);
        Manager.junitSetDateNow(date);
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testRightDayRightTimeSelectDays() {
        final SprinklerProgram prog = parseProgram(this.selectDaysStr);
        final Date date = getDay(new Date(2013/*year*/, 7/*month*/, 15/*date*/, 15/*hrs*/, 31/*min*/), DateTimeConstants.THURSDAY);
        Manager.junitSetDateNow(date);
        assertEquals(Zone.ZONE_3, prog.getActiveZone());
    }

    @Test
    public void testWrongDayRightTime() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.junitSetRecentCompletion(new Date());
        final DateTime dateTime = new DateTime().minus(100*1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new Date());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testDayPassedWrongTime() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.junitSetRecentCompletion(new Date(1L));
        prog.setStartTime(new SprinklerProgram.ActivationTime(1,0));
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testTooEarlyToday() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.setRepeat(new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.DAILY, null));
        final DateTime dateTime = new DateTime().plus(100 * 1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new Date());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testTooLateToday() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.setRepeat(new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.DAILY, null));
        final DateTime dateTime = new DateTime().minus(100*1000*60);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new Date());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testOnTime() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.setRepeat(new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.DAILY, null));
        final DateTime dateTime = new DateTime().minus(14*1000*60);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new Date());
        assertEquals(Zone.ZONE_3, prog.getActiveZone());
    }

//    @Test
    public void createSampleProgramXml() {
        final ProgramSet programSet = new ProgramSet();
        final SprinklerProgram program = new SprinklerProgram();
        final DateTime dateTime = new DateTime();
        program.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        final List<SprinklerRepeatPolicy.DayOfWeek> days = new ArrayList<SprinklerRepeatPolicy.DayOfWeek>(2);
        days.add(SprinklerRepeatPolicy.DayOfWeek.SUNDAY);
        days.add(SprinklerRepeatPolicy.DayOfWeek.THURSDAY);
        program.setRepeat(new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.SELECT_DAYS, days));
        final SprinklerAction action1 = new SprinklerAction(10, Zone.ZONE_2);
        final SprinklerAction action2 = new SprinklerAction(15, Zone.ZONE_3);
        program.addAction(action1);
        program.addAction(action2);
        programSet.addProgram(program);

        System.out.println(programSet.toXml());
    }

    private Date getDay(final Date rightTimeWrongDay, final /*DateTimeConstants*/int day) {
        DateTime dateTime = new DateTime(rightTimeWrongDay.getTime());
        while(dateTime.getDayOfWeek() != day) {
            dateTime = dateTime.minusDays(1);
        }
        return new Date(dateTime.getMillis());
    }
}
