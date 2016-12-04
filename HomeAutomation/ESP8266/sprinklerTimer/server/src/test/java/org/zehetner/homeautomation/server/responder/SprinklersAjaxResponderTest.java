package org.zehetner.homeautomation.server.responder;

import org.junit.Test;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.mock.MockHttpServletRequest;
import org.zehetner.homeautomation.sprinklers.Zone;
import org.zehetner.homeautomation.stateengine.SprinklerProgram;
import org.zehetner.homeautomation.stateengine.SprinklerRepeatPolicy;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 3/29/2016
 * Time: 8:21 PM
 */
public class SprinklersAjaxResponderTest {

    @Test
    public void testOnDemand() throws Exception {
        SprinklersAjaxResponder responder = new SprinklersAjaxResponder();
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.setRequestURI("http://localhost/house/sprinklersAjax/activateZone");
        request.getParameterMap().put("zone", new String[] {"ZONE_3"});
        responder.respond(request);

        assertEquals(Zone.ZONE_3, Manager.getSingleton().getSprinklerStateEngine().getOnDemandZoneState());
    }

    @Test
    public void testEnable() throws Exception {
        SprinklersAjaxResponder responder = new SprinklersAjaxResponder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        SprinklerProgram sp = new SprinklerProgram();
        final String programName = "sprinkler_program";
        sp.setName(programName);
        Manager.getSingleton().getProgramSet().addProgram(sp);

        request.setRequestURI("http://localhost/house/sprinklersAjax/setProgramEnable");
        request.getParameterMap().put("programName", new String[]{programName});
        request.getParameterMap().put("value", new String[]{"true"});
        responder.respond(request);

        assertTrue(Manager.getSingleton().getProgramSet().getProgram(programName).isEnabled());
    }

    @Test
    public void testRunNow() throws Exception {
        Manager.getSingleton().getProgramSet().junitClearPrograms();
        SprinklersAjaxResponder responder = new SprinklersAjaxResponder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        SprinklerProgram sp = new SprinklerProgram();
        final String programName = "sprinkler_program";
        sp.setName(programName);
        sp.setRepeat(new SprinklerRepeatPolicy(SprinklerRepeatPolicy.Type.EVEN_DAYS, null));
        Manager.getSingleton().getProgramSet().addProgram(sp);

        request.setRequestURI("http://localhost/house/sprinklersAjax/runNow");
        request.getParameterMap().put("programName", new String[]{programName});
        responder.respond(request);

        assertTrue(Manager.getSingleton().getProgramSet().getProgram(programName).getOnDemandStartTime() != null);
    }

    @Test
    public void testMultiplier() throws Exception {
        SprinklersAjaxResponder responder = new SprinklersAjaxResponder();
        MockHttpServletRequest request = new MockHttpServletRequest();
        SprinklerProgram sp = new SprinklerProgram();
        final String programName = "sprinkler%20program";
        sp.setName(programName);
        Manager.getSingleton().getProgramSet().addProgram(sp);

        request.setRequestURI("http://localhost/house/sprinklersAjax/setProgramMultiplier");
        request.getParameterMap().put("programName", new String[]{programName});
        request.getParameterMap().put("value", new String[]{"200"});
        responder.respond(request);

        assertEquals(200, Manager.getSingleton().getProgramSet().getProgram(programName).getMultiplier());
    }
}
