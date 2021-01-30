package org.zehetner.homeautomation.server.servlets;

import junit.framework.TestCase;
import org.junit.Test;
import org.zehetner.homeautomation.server.responder.HvacAjaxResponder;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/21/11
 * Time: 9:22 PM
 */
@SuppressWarnings({"ClassWithoutLogger"})
public class HvacAjaxServletTest extends TestCase {
    @Test
    public void testResponderType() {
        assertTrue("Wrong responder type", new HvacAjaxServlet().getResponder() instanceof HvacAjaxResponder);
    }

    @Test
    public void testContentType() {
        assertEquals("Wrong content type", "application/json", new HvacAjaxServlet().getContentType());
    }
}
