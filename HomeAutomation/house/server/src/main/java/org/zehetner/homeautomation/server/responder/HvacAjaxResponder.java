package org.zehetner.homeautomation.server.responder;

import org.json.JSONException;
import org.json.JSONObject;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.hvac.FahrenheitTemperature;
import org.zehetner.homeautomation.hvac.HvacSettings;
import org.zehetner.homeautomation.hvac.Mode;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class HvacAjaxResponder implements PageResponder {
    public static final String TEMP_CHANGE_ACTION = "tempChangeAction";
    public static final String MODE_CHANGE_ACTION = "modeChangeAction";
    public static final String FAN_CHANGE_ACTION = "fanChangeAction";
    public static final String BTN_UP = "incrTemp";
    public static final String BTN_DOWN = "decrTemp";
    public static final String COOL_MODE = "coolMode";
    public static final String OFF_MODE = "offMode";
    public static final String HEAT_MODE = "heatMode";
    public static final String SET_TEMP_FIELD = "setTempField";

	@Override
    public String respond(final HttpServletRequest request) throws IOException, JSONException {

        HvacSettings settings = Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings();

		String btnPressed = request.getParameter(TEMP_CHANGE_ACTION);
        if (btnPressed != null) {
            double setTempF = settings.getHoldTemperature().getFahrenheitTemperature();
            if (BTN_UP.equals(btnPressed)) {
                setTempF++;
            } else if (BTN_DOWN.equals(btnPressed)) {
                setTempF--;
            }
            settings.setHoldTemperature(new FahrenheitTemperature(setTempF));
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put(SET_TEMP_FIELD, setTempF);
            return jsonObject.toString();
        }

        btnPressed = request.getParameter(MODE_CHANGE_ACTION);
        if (btnPressed != null) {
            settings.setMode(Mode.valueOf(btnPressed));
            return "";
        }

        btnPressed = request.getParameter(FAN_CHANGE_ACTION);
        if (btnPressed != null) {
            settings.setFanOn("true".equals(btnPressed));
            return "";
        }

        return "";
	}
}
