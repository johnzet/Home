package org.zehetner.homeautomation.server.responder;

import junit.framework.TestCase;
import org.json.JSONException;
import org.junit.Test;
import org.zehetner.homeautomation.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class RootPageResponderTest extends TestCase {

	@Test
	public void testRootPageContent() throws IOException, JSONException {
        final HttpServletRequest request = new MockHttpServletRequest();
		final PageResponder responder = new RootPageResponder();
		final String result = responder.respond(request);
		assertTrue(result.contains("www.wunderground.com"));
	}
}
