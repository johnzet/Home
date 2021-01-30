package org.zehetner.homeautomation.server.responder;

import org.json.JSONException;
import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.hvac.FahrenheitTemperature;
import org.zehetner.homeautomation.hvac.Mode;
import org.zehetner.homeautomation.mock.MockHttpServletRequest;
import org.zehetner.homeautomation.server.servlets.HvacAjaxServlet;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 9:30 PM
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class HvacAjaxResponderTest {

    @Test
    public void testTempUpButton() throws IOException, JSONException {
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings().setMode(Mode.HEAT);
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings().setHoldTemperature(new FahrenheitTemperature(41.0));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(HvacAjaxResponder.TEMP_CHANGE_ACTION, HvacAjaxResponder.BTN_UP);
        final String response = new HvacAjaxServlet().getResponder().respond(request);
        assertTrue(response.contains("42"));
    }

    @Test
    public void testTempDownButton() throws IOException, JSONException {
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings().setMode(Mode.HEAT);
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings().setHoldTemperature(new FahrenheitTemperature(43.0));
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(HvacAjaxResponder.TEMP_CHANGE_ACTION, HvacAjaxResponder.BTN_DOWN);
        final String response = new HvacAjaxServlet().getResponder().respond(request);
        assertTrue(response.contains("42"));
    }

    @Test
    public void testHeatModeButton() throws IOException, JSONException {
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacSettings().setMode(Mode.OFF);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(HvacAjaxResponder.TEMP_CHANGE_ACTION, HvacAjaxResponder.HEAT_MODE);
        final String response = new HvacAjaxServlet().getResponder().respond(request);
//        assertTrue(response.contains("HEAT"));
        assertFalse(response.contains("OFF"));
    }
}
