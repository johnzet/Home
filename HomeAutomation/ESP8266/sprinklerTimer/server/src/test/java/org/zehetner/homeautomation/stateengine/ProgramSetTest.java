package org.zehetner.homeautomation.stateengine;

import org.junit.Test;
import org.zehetner.homeautomation.sprinklers.Zone;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/23/12
 * Time: 7:01 PM
 */
public class ProgramSetTest {
    private final String every3daysStr = "<list>\n" +
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
            "        <durationMinutes>11</durationMinutes>\n" +
            "        <zone>ZONE_3</zone>\n" +
            "      </SprinklerAction>\n" +
            "      <SprinklerAction>\n" +
            "        <durationMinutes>15</durationMinutes>\n" +
            "        <zone>ZONE_7</zone>\n" +
            "      </SprinklerAction>\n" +
            "    </actions>\n" +
            "  </SprinklerProgram>\n" +
            "</list>";

    private final String selectDaysStr = "<list>\n" +
            "  <SprinklerProgram>\n" +
            "    <name>Program A</name>\n" +
            "    <enabled>false</enabled>\n" +
            "    <multiplier>100</multiplier>\n" +
            "    <repeat>\n" +
            "      <type>SELECT_DAYS</type>\n" +
            "      <optionalDaysInterval>1</optionalDaysInterval>\n" +
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
            "        <durationMinutes>11</durationMinutes>\n" +
            "        <zone>ZONE_3</zone>\n" +
            "      </SprinklerAction>\n" +
            "      <SprinklerAction>\n" +
            "        <durationMinutes>15</durationMinutes>\n" +
            "        <zone>ZONE_7</zone>\n" +
            "      </SprinklerAction>\n" +
            "    </actions>\n" +
            "  </SprinklerProgram>\n" +
            "</list>";

    @Test
    public void testToXmlPeriodic() {
        final SprinklerAction sa1 = new SprinklerAction(11, Zone.ZONE_3);
        final SprinklerAction sa2 = new SprinklerAction(15, Zone.ZONE_7);
        final SprinklerProgram sp1 = new SprinklerProgram();
        sp1.setName("Program A");
        sp1.setEnabled(false);
        SprinklerRepeatPolicy repeat = new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.EVERY_N_DAYS, null);
        repeat.setOptionalDaysInterval(3);
        sp1.setRepeat(repeat);

        sp1.setStartTime(new SprinklerProgram.ActivationTime(4, 30));
        sp1.addAction(sa1);
        sp1.addAction(sa2);

        final ProgramSet ps1 = new ProgramSet();
        ps1.addProgram(sp1);

        final String xmlStr = ps1.toXml();
        assertEquals(this.every3daysStr, xmlStr);
    }

    @Test
    public void testFromXmlPeriodic() {
        final ProgramSet ps1 = new ProgramSet();
        ps1.loadFromXml(this.every3daysStr);

        assertEquals(1L, (long)ps1.getPrograms().size());
        final SprinklerProgram sp1 = (SprinklerProgram)ps1.getPrograms().get(0);
        assertEquals("Program A", sp1.getName());
        assertEquals(false, sp1.isEnabled());
        assertEquals(SprinklerRepeatPolicy.Type.EVERY_N_DAYS, sp1.getRepeat().getType());
        assertEquals(3, sp1.getRepeat().getOptionalDaysInterval());
        assertEquals(0L, (long)sp1.getRepeat().getDayList().size());
        assertEquals(4L, (long)sp1.getStartTime().getHour());
        assertEquals(30L, (long)sp1.getStartTime().getMinute());

        assertEquals(2L, (long)sp1.getActions().size());
        final SprinklerAction sa1 = sp1.getActions().get(0);
        final SprinklerAction sa2 = sp1.getActions().get(1);
        assertEquals(11L, (long)sa1.getDurationMinutes());
        assertEquals(15L, (long)sa2.getDurationMinutes());
        assertEquals(Zone.ZONE_3, sa1.getZone());
        assertEquals(Zone.ZONE_7, sa2.getZone());
    }

    @Test
    public void testToXmlSelectDays() {
        final SprinklerAction sa1 = new SprinklerAction(11, Zone.ZONE_3);
        final SprinklerAction sa2 = new SprinklerAction(15, Zone.ZONE_7);
        final SprinklerProgram sp1 = new SprinklerProgram();
        sp1.setName("Program A");
        sp1.setEnabled(false);
        List<SprinklerRepeatPolicy.DayOfWeek> days = new ArrayList<SprinklerRepeatPolicy.DayOfWeek>(2);
        days.add(SprinklerRepeatPolicy.DayOfWeek.SUNDAY);
        days.add(SprinklerRepeatPolicy.DayOfWeek.THURSDAY);
        sp1.setRepeat(new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.SELECT_DAYS, days));
        sp1.setStartTime(new SprinklerProgram.ActivationTime(15, 20));
        sp1.addAction(sa1);
        sp1.addAction(sa2);


        final ProgramSet ps1 = new ProgramSet();
        ps1.addProgram(sp1);

        final String xmlStr = ps1.toXml();
        assertEquals(this.selectDaysStr, xmlStr);
    }

    @Test
    public void testFromXmlSelectDays() {
        final ProgramSet ps1 = new ProgramSet();
        ps1.loadFromXml(this.selectDaysStr);

        assertEquals(1L, (long)ps1.getPrograms().size());
        final SprinklerProgram sp1 = (SprinklerProgram)ps1.getPrograms().get(0);
        assertEquals("Program A", sp1.getName());
        assertEquals(false, sp1.isEnabled());
        assertEquals(SprinklerRepeatPolicy.Type.SELECT_DAYS, sp1.getRepeat().getType());
        assertEquals(2L, (long)sp1.getRepeat().getDayList().size());
        assertTrue(sp1.getRepeat().getDayList().contains(SprinklerRepeatPolicy.DayOfWeek.SUNDAY));
        assertTrue(sp1.getRepeat().getDayList().contains(SprinklerRepeatPolicy.DayOfWeek.THURSDAY));
        assertEquals(15L, (long) sp1.getStartTime().getHour());
        assertEquals(20L, (long)sp1.getStartTime().getMinute());

        assertEquals(2L, (long)sp1.getActions().size());
        final SprinklerAction sa1 = sp1.getActions().get(0);
        final SprinklerAction sa2 = sp1.getActions().get(1);
        assertEquals(11L, (long)sa1.getDurationMinutes());
        assertEquals(15L, (long)sa2.getDurationMinutes());
        assertEquals(Zone.ZONE_3, sa1.getZone());
        assertEquals(Zone.ZONE_7, sa2.getZone());
    }
}
