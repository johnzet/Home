package org.zehetner.homeautomation.server.responder;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.hvac.CelsiusTemperature;
import org.zehetner.homeautomation.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

public class HvacPageResponderTest {

	@Test
	public void testHvacPageContent() throws IOException, JSONException {
	    final HttpServletRequest request = new MockHttpServletRequest();
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacMechanical().init();
		final PageResponder responder = new HvacPageResponder();
		final String result = responder.respond(request);

		Assert.assertThat(result, JUnitMatchers.containsString("Zehetner House HVAC"));
	}

    @Test
    public void testHvacPageContentDeadThermostat() throws IOException, JSONException {
        Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacMechanical().init();
        Manager.getSingleton().getSensors().setIndoorTemperature(new CelsiusTemperature(42.0));
        Manager.junitSetDateNow(new Date(System.currentTimeMillis() + (6L * 60L * 1000L)));


        final HttpServletRequest request = new MockHttpServletRequest();
        final PageResponder responder = new HvacPageResponder();
        final String result = responder.respond(request);

        Assert.assertThat(result, JUnitMatchers.containsString("Zehetner House HVAC"));
        Assert.assertThat(result, JUnitMatchers.containsString("THE SYSTEM IS OFF!!!"));
    }
}
