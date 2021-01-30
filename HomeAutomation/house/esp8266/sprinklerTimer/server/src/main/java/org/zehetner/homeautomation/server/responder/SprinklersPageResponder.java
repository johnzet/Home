package org.zehetner.homeautomation.server.responder;

import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;
import org.zehetner.homeautomation.stateengine.Program;
import org.zehetner.homeautomation.stateengine.ProgramSet;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

public class SprinklersPageResponder implements PageResponder {

    public String respond(final HttpServletRequest request) throws IOException {


        final StringBuilder sprinklersHtml = new StringBuilder(4096);
		final String path = "sprinklers.html";

        final InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path));
        try {

	        final char[] buf = new char[1024];

	        int r;

	        while ((r = reader.read(buf)) != -1) {
                sprinklersHtml.append(buf, 0, r);
	        }
        }
	    finally {
            reader.close();
	    }
		return MessageFormat.format(sprinklersHtml.toString(), buildOptionsList(), buildProgramsList());
	}

    private String buildOptionsList() {
        final StringBuilder sb = new StringBuilder(500);
        addOption(sb, Zone.ALL_OFF);
        addOption(sb, Zone.ZONE_1);
        addOption(sb, Zone.ZONE_2);
        addOption(sb, Zone.ZONE_3);
        addOption(sb, Zone.ZONE_4);
        addOption(sb, Zone.ZONE_5);
        addOption(sb, Zone.ZONE_6);
        addOption(sb, Zone.ZONE_7);
        addOption(sb, Zone.ZONE_8);

        return sb.toString();
    }

    private void addOption(final StringBuilder sb, final Zone zone) {
        sb.append("<option value=\"");
        sb.append(zone.toString());
        sb.append("\">");
        sb.append((zone == Zone.ALL_OFF) ? "Off" : zone.getName());
        sb.append("</option>");
    }

    private String buildProgramsList() {
        final StringBuilder sb = new StringBuilder(500);
        ProgramSet programs = Manager.getSingleton().getProgramSet();
        for (Program program : programs.getPrograms()) {
            if (program instanceof SprinklerProgram) {
                SprinklerProgram sprinklerProgram = (SprinklerProgram)program;
                addProgramRow(sb, sprinklerProgram);
            }
        }
        return sb.toString();
    }

    private void addProgramRow(final StringBuilder sb, final SprinklerProgram sprinklerProgram) {
        sb.append("<tr>");
        sb.append("<td><input type=\"checkbox\" onchange=\"setProgramEnable('").append(sprinklerProgram.getName()).append("', this)\" ");
        if (sprinklerProgram.isEnabled()) sb.append("checked=\"true\"");
        sb.append("/></td>");
        sb.append("<td>").append(sprinklerProgram.getName()).append("</td>");
        sb.append("<td>").append("<input type=\"number\" min=\"10\" max=\"500\" step=\"10\" value=\"")
                .append(sprinklerProgram.getMultiplier()).append("\" size=\"3\" onchange=\"setProgramMultiplier('").append(sprinklerProgram.getName()).append("', this)\"/>%</td>");
        sb.append("<td><input type=\"button\" value=\"Run Now\" onclick=\"runNow('").append(sprinklerProgram.getName()).append("')\"/></td>");
        sb.append("</tr>\n");
    }
}
