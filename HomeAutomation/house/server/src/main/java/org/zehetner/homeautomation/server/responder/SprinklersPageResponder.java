package org.zehetner.homeautomation.server.responder;

import org.zehetner.homeautomation.sprinklers.Zone;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

public class SprinklersPageResponder implements PageResponder {

    @Override
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
		return MessageFormat.format(sprinklersHtml.toString(), buildOptionsList());
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
        sb.append((zone == Zone.ALL_OFF)? "Off" : zone.getName());
        sb.append("</option>");
    }

}
