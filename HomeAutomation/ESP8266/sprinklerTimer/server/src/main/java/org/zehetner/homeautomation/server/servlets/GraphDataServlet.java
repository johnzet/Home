package org.zehetner.homeautomation.server.servlets;

import org.apache.log4j.Logger;
import org.zehetner.homeautomation.server.responder.GraphDataResponder;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/21/2016
 * Time: 2:25 PM
 */

public class GraphDataServlet extends AbstractServlet {
    private static final Logger LOG = Logger.getLogger(GraphDataServlet.class.getName());

    public GraphDataServlet() {
        super();
        setResponder(new GraphDataResponder());
    }

}
