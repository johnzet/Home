package org.zehetner.homeautomation.server.servlets;

import org.apache.log4j.Logger;
import org.zehetner.homeautomation.server.responder.GraphResponder;


/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/21/2016
 * Time: 2:25 PM
 */


public class GraphServlet extends AbstractServlet {
    private static final Logger LOG = Logger.getLogger(GraphServlet.class.getName());
    private static final long serialVersionUID = -4058624688654817688L;

    public GraphServlet() {
        super();
        setResponder(new GraphResponder());
    }

}
