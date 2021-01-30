package org.zehetner.homeautomation.server.responder;

import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.hvac.Equipment;
import org.zehetner.homeautomation.hvac.HvacSettings;
import org.zehetner.homeautomation.hvac.HvacStateEngine;
import org.zehetner.homeautomation.hvac.Mode;
import org.zehetner.homeautomation.hvac.Sensors;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;

public class HvacPageResponder implements PageResponder {

    @Override
    public String respond(final HttpServletRequest request) throws IOException {
		final Sensors sensors = Manager.getSingleton().getSensors();
        final HvacStateEngine hvacStateEngine = Manager.getSingleton().getHvacSystem().getHvacStateEngine();
        final HvacSettings hvacSettings = hvacStateEngine.getHvacSettings();

        final String currentTempFStr = String.format("%.1f", sensors.getIndoorTemperature().getFahrenheitTemperature());
        final String currentHumidityStr = String.format("%.1f", sensors.getIndoorHumidity());
        final double setTempF = hvacSettings.getHoldTemperature().getFahrenheitTemperature();
        final String setTempFStr = String.format("%.0f", setTempF);
        final String outdoorTempFStr = String.format("%.1f", sensors.getOutdoorTemperature().getFahrenheitTemperature());
        final String outdoorHumidityStr = String.format("%.1f", sensors.getOutdoorHumidity());
        final String barometerKpaStr = String.format("%.2f", sensors.getBarometer());
        final String fanOn = (hvacSettings.isFanOn())? "checked='checked'" : "";

        final String message;
        if (! sensors.isIndoorTemperatureRecent()) {
            message = "NO RECENT THERMOSTAT COMMUNICATION - THE SYSTEM IS OFF!!!!";
        } else if (hvacStateEngine.isOn(Equipment.HEAT_1) || hvacStateEngine.isOn(Equipment.HEAT_2)) {
            message = "Heat is On";
        } else if (hvacStateEngine.isOn(Equipment.COOL_1) || hvacStateEngine.isOn(Equipment.COOL_2)) {
            message = "A/C is On";
        } else {
            message = "";
        }

        final String modeOff = (Mode.OFF == hvacSettings.getMode())? "selected" : "";
        final String modeHeat = (Mode.HEAT == hvacSettings.getMode())? "selected" : "";
        final String modeCool = (Mode.COOL == hvacSettings.getMode())? "selected" : "";

        final StringBuilder hvacHtml = new StringBuilder(4096);
		final String path = "hvac.html";

        final InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path));
        try {

	        final char[] buf = new char[1024];

	        int r = 0;

	        while ((r = reader.read(buf)) != -1) {
	            hvacHtml.append(buf, 0, r);
	        }
        }
	    finally {
            reader.close();
	    }
		return MessageFormat.format(hvacHtml.toString(), currentTempFStr, currentHumidityStr,
                outdoorTempFStr, outdoorHumidityStr, barometerKpaStr,
                setTempFStr, message,
                modeOff, modeHeat, modeCool, fanOn);
	}

}
