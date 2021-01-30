package org.zehetner.homeautomation.stateengine;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertFalse;
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
            "    <enabled>true</enabled>\n" +
            "    <multiplier>100</multiplier>\n" +
            "    <repeat>\n" +
            "      <type>EVERY_N_DAYS</type>\n" +
            "      <optionalDaysInterval>3</optionalDaysInterval>\n" +
            "      <dayList/>\n" +
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

    private final String disabledStr = "<list>\n" +
            "  <SprinklerProgram>\n" +
            "    <name>Program A</name>\n" +
            "    <enabled>false</enabled>\n" +
            "    <multiplier>100</multiplier>\n" +
            "    <repeat>\n" +
            "      <type>EVERY_N_DAYS</type>\n" +
            "      <optionalDaysInterval>3</optionalDaysInterval>\n" +
            "      <dayList/>\n" +
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

    private final String multiplierStr = "<list>\n" +
            "  <SprinklerProgram>\n" +
            "    <name>Program A</name>\n" +
            "    <enabled>true</enabled>\n" +
            "    <multiplier>200</multiplier>\n" +
            "    <repeat>\n" +
            "      <type>EVERY_N_DAYS</type>\n" +
            "      <optionalDaysInterval>3</optionalDaysInterval>\n" +
            "      <dayList/>\n" +
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
            "    <enabled>true</enabled>\n" +
            "    <multiplier>100</multiplier>\n" +
            "    <repeat>\n" +
            "      <type>SELECT_DAYS</type>\n" +
            "      <dayList>\n" +
            "        <DayOfWeek>SUNDAY</DayOfWeek>\n" +
            "        <DayOfWeek>THURSDAY</DayOfWeek>\n" +
            "      </dayList>\n" +
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
        final DateTime date = new DateTime(2016, 3, 15, 15, 31);  // Tuesday
        Manager.junitSetDateNow(date);
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testRightDayRightTimeSelectDays() {
        for (int i=0; i<7; i++) {
            runRightDayRightTimeSelectDays(i);
        }
    }

    private void runRightDayRightTimeSelectDays(int dayOffset) {
        final SprinklerProgram prog = parseProgram(this.selectDaysStr);
        final DateTime date = new DateTime(2016, 3, 13+dayOffset, 15, 31);
        prog.getRepeat().getDayList().clear();
        prog.getRepeat().getDayList().add(SprinklerRepeatPolicy.DayOfWeek.values()[dayOffset]);
        Manager.junitSetDateNow(date);
        assertEquals(Zone.ZONE_3, prog.getActiveZone());
    }

    @Test
    public void runProgramOnDemand() {
        DateTime startTime = new DateTime().plusMinutes(10);
        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.junitClearPrograms();
        SprinklerProgram prog1 = parseProgram(this.selectDaysStr);
        prog1.setName("prog1");
        prog1.setStartTime(new SprinklerProgram.ActivationTime(startTime.getHourOfDay(), startTime.getMinuteOfHour()));
        progSet.addProgram(prog1);
        SprinklerProgram prog2 = parseProgram(this.selectDaysStr);
        prog2.setName("prog2");
        prog2.setStartTime(new SprinklerProgram.ActivationTime(startTime.getHourOfDay(), startTime.getMinuteOfHour()));
        prog2.setMultiplier(70);
        progSet.addProgram(prog2);
        prog2.setOnDemandStartTime(Manager.getDateNow());

        assertEquals(Zone.ZONE_2, prog2.getActiveZone());

        assertEquals(2, progSet.getPrograms().size());
        Manager.junitSetDateNow(new DateTime().plusHours(1));
        assertEquals(Zone.ALL_OFF, prog2.getActiveZone());
        assertEquals(2, progSet.getPrograms().size());
        assertEquals(null, prog2.getOnDemandStartTime());
    }

    @Test
    public void runProgramOnDemandThenDisable() {
        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.junitClearPrograms();
        SprinklerProgram prog = parseProgram(this.selectDaysStr);
        prog.setName("prog1");
        progSet.addProgram(prog);
        prog.setOnDemandStartTime(Manager.getDateNow());

        Manager.junitSetDateNow(Manager.getDateNow().plusMinutes(5));
        assertEquals(Zone.ZONE_2, prog.getActiveZone());
        assertEquals(1, progSet.getPrograms().size());

        prog.setEnabled(false);
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
        assertEquals(1, progSet.getPrograms().size());
        assertEquals(null, prog.getOnDemandStartTime());
    }

    @Test
    public void runDisabledProgramOnDemand() {
        final ProgramSet progSet = Manager.getSingleton().getProgramSet();
        progSet.junitClearPrograms();
        SprinklerProgram prog = parseProgram(this.selectDaysStr);
        prog.setName("prog1");
        prog.setEnabled(false);
        progSet.addProgram(prog);
        prog.setOnDemandStartTime(Manager.getDateNow());

        Manager.junitSetDateNow(Manager.getDateNow().plusMinutes(5));
        assertEquals(Zone.ZONE_2, prog.getActiveZone());
        assertEquals(1, progSet.getPrograms().size());

        prog.setEnabled(false);
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
        assertEquals(1, progSet.getPrograms().size());
        assertEquals(null, prog.getOnDemandStartTime());
        assertFalse(prog.isEnabled());
    }

    @Test
    public void runRightDayRightTimeSelectDaysAfterDisable() {
        final SprinklerProgram prog = parseProgram(this.selectDaysStr);

        final DateTime date = new DateTime(2016, 3, 13, 15, 31);
        prog.getRepeat().getDayList().clear();
        prog.getRepeat().getDayList().add(SprinklerRepeatPolicy.DayOfWeek.values()[0]);
        Manager.junitSetDateNow(date);
        assertEquals(Zone.ZONE_3, prog.getActiveZone());
        prog.setEnabled(false);
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testRightDayRightTimeDisabled() {
        final SprinklerProgram prog = parseProgram(this.disabledStr);
        final DateTime dateTime = new DateTime().minus(100 * 1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());  // tests recentCompletion handling
    }

    @Test
    public void testRightDayRightTimeWithMultiplierGreaterThan100() {
        final SprinklerProgram prog = parseProgram(this.multiplierStr);
        final DateTime dateTime = new DateTime().minus(12 * 60 * 1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ZONE_2, prog.getActiveZone());
        assertEquals(Zone.ZONE_2, prog.getActiveZone());  // tests recentCompletion handling
    }

    @Test
    public void testRightDayRightTimeWithMultiplierLessThan100() {
        final SprinklerProgram prog = parseProgram(this.multiplierStr);
        final DateTime dateTime = new DateTime().minus(6 * 60 * 1000);
        prog.setMultiplier(50);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ZONE_3, prog.getActiveZone());
        assertEquals(Zone.ZONE_3, prog.getActiveZone());  // tests recentCompletion handling
    }

    @Test
    public void testRightDayRightTimeSecondPass() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        final DateTime dateTime = new DateTime().minus(100 * 1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ZONE_2, prog.getActiveZone());
        assertEquals(Zone.ZONE_2, prog.getActiveZone());  // tests recentCompletion handling
    }

    @Test
    public void testWrongDayRightTime() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.junitSetRecentCompletion(new DateTime());
        final DateTime dateTime = new DateTime().minus(100 * 1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());  // tests recentCompletion handling
    }

    @Test
    public void testDayPassedWrongTime() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        prog.junitSetRecentCompletion(new DateTime(1L));
        prog.setStartTime(new SprinklerProgram.ActivationTime(1, 0));
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());  // tests recentCompletion handling
    }

    @Test
    public void testTooEarlyToday() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        SprinklerRepeatPolicy repeat = new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.EVERY_N_DAYS, null);
        repeat.setOptionalDaysInterval(1);
        prog.setRepeat(repeat);
        final DateTime dateTime = new DateTime().plus(100 * 1000);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testTooLateToday() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        SprinklerRepeatPolicy repeat = new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.EVERY_N_DAYS, null);
        repeat.setOptionalDaysInterval(1);
        prog.setRepeat(repeat);
        final DateTime dateTime = new DateTime().minus(100 * 1000 * 60);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
    }

    @Test
    public void testOnTime() {
        final SprinklerProgram prog = parseProgram(this.every3daysStr);
        SprinklerRepeatPolicy repeat = new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.EVERY_N_DAYS, null);
        repeat.setOptionalDaysInterval(1);
        prog.setRepeat(repeat);
        final DateTime dateTime = new DateTime().minus(14 * 1000 * 60);
        prog.setStartTime(new SprinklerProgram.ActivationTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour()));
        Manager.junitSetDateNow(new DateTime());
        assertEquals(Zone.ZONE_3, prog.getActiveZone());
    }

    @Test
    public void testOddDays() {
        runOddOrEvenDaysTest(true);
    }

    @Test
    public void testEvenDays() {
        runOddOrEvenDaysTest(false);
    }

    private void runOddOrEvenDaysTest(final boolean isOddDays) {
        final String progStr = "<list>\n" +
                "  <SprinklerProgram>\n" +
                "    <name>Program A</name>\n" +
                "    <enabled>true</enabled>\n" +
                "    <multiplier>100</multiplier>\n" +
                "    <repeat>\n" +
                (isOddDays? "<type>ODD_DAYS</type>\n" : "<type>EVEN_DAYS</type>\n") +
                "      <recentCompletion>1970-01-01 00:00:00.0 UTC</recentCompletion>\n" +
                "    </repeat>\n" +
                "    <startTime>\n" +
                "      <hour>5</hour>\n" +
                "      <minute>0</minute>\n" +
                "    </startTime>\n" +
                "    <actions>\n" +
                "      <SprinklerAction>\n" +
                "        <durationMinutes>10</durationMinutes>\n" +
                "        <zone>ZONE_1</zone>\n" +
                "      </SprinklerAction>\n" +
                "    </actions>\n" +
                "  </SprinklerProgram>\n" +
                "</list>\n";

        final SprinklerProgram prog = parseProgram(progStr);
        DateTime now = new DateTime(2016, 3, 10, prog.getStartTime().getHour(), prog.getStartTime().getMinute()+1, DateTimeZone.getDefault());

        if (isOddDays) now = now.plusDays(1);
        Manager.junitSetDateNow(now);
        assertEquals(Zone.ZONE_1, prog.getActiveZone());

        now = now.plusDays(1);
        Manager.junitSetDateNow(now);
        assertEquals(Zone.ALL_OFF, prog.getActiveZone());
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
