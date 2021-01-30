package org.zehetner.homeautomation.server.responder;

import org.json.JSONException;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.sprinklers.Zone;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class SprinklersAjaxResponder implements PageResponder {
    public static final String ACTIVATE_ZONE_ACTION = "activateZone";

	@Override
    public String respond(final HttpServletRequest request) throws IOException, JSONException {

        final String btnPressed = request.getParameter(ACTIVATE_ZONE_ACTION);
        if (btnPressed != null) {
            Manager.getSingleton().getSprinklerStateEngine().setOnDemandZoneState(Zone.valueOf(btnPressed));
            return "";
        }

        return "";
	}
}
