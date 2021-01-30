package org.zehetner.homeautomation.server.responder;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class SprinklersAjaxResponder implements PageResponder {
    private static final Logger LOG = Logger.getLogger(SprinklersAjaxResponder.class.getName());
    private Map<String, String[]> queryParams;

    public String respond(final HttpServletRequest request) throws IOException, JSONException {

        this.queryParams = request.getParameterMap();
        String response;
        if (request.getRequestURI().contains("sprinklersAjax/activateZone")) {
            response = handleActivateZone();
        } else if (request.getRequestURI().contains("sprinklersAjax/setProgramEnable")) {
            response = handleSetEnabled();
        } else if (request.getRequestURI().contains("sprinklersAjax/setProgramMultiplier")) {
            response = handleSetMultiplier();
        } else if (request.getRequestURI().contains("sprinklersAjax/runNow")) {
            response = handleRunNow();
        } else {
            throw new IllegalStateException("Dev error");
        }


        return response;
	}

    private String handleActivateZone() {
        final String zoneValStr = getFirstValue("zone");
        if (zoneValStr != null && zoneValStr.length() > 0) {
            Manager.getSingleton().getSprinklerStateEngine().setOnDemandZoneState(Zone.valueOf(zoneValStr));
        }
        return "";
    }

    private String handleSetEnabled() {
        final String name = getFirstValue("programName");
        final String value = getFirstValue("value");
        SprinklerProgram sp = Manager.getSingleton().getProgramSet().getProgram(name);
        if (sp != null) {
            LOG.info("Setting enable to " + value + " for program " + name);
            sp.setEnabled(Boolean.parseBoolean(value));
        }
        Manager.getSingleton().getProgramSet().savePrograms();
        return "";
    }

    private String handleSetMultiplier() {
        final String name = getFirstValue("programName");
        final String value = getFirstValue("value");
        SprinklerProgram sp = Manager.getSingleton().getProgramSet().getProgram(name);
        if (sp != null) {
            LOG.info("Setting multiplier to " + value + " for program " + name);
            sp.setMultiplier(Integer.parseInt(value));
        }
        Manager.getSingleton().getProgramSet().savePrograms();
        return "";
    }

    private String handleRunNow() {
        final String name = getFirstValue("programName");
        SprinklerProgram sp = Manager.getSingleton().getProgramSet().getProgram(name);
        if (sp != null) {
            LOG.info("Starting program " + name);
            sp.setOnDemandStartTime(Manager.getDateNow());
        }
        return "";
    }

    private String getFirstValue(final String key) {
        if (this.queryParams == null) return "";
        final String[] valStrs = this.queryParams.get(key);
        if (valStrs == null || valStrs.length < 1) return "";
        return valStrs[0];
    }
}
