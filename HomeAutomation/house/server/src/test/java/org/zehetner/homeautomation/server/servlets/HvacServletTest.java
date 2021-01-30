package org.zehetner.homeautomation.server.servlets;

import junit.framework.TestCase;
import org.zehetner.homeautomation.mock.MockHttpServletRequest;
import org.zehetner.homeautomation.mock.MockHttpServletResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class HvacServletTest extends TestCase {

	public void testRequest() throws ServletException, IOException {
		final HvacServlet servlet = new HvacServlet();
		final MockHttpServletRequest request = new MockHttpServletRequest();
		final MockHttpServletResponse response = new MockHttpServletResponse();
		servlet.doGet(request, response);
		assertEquals("utf-8", response.getCharacterEncoding());
		assertEquals("text/html", response.getContentType());
		assertEquals(HttpServletResponse.SC_OK, response.getStatus());
	}
}
