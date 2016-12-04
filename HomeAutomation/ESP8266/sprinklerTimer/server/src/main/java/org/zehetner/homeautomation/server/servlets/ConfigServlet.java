package org.zehetner.homeautomation.server.servlets;

import org.apache.log4j.Logger;
import org.zehetner.homeautomation.server.responder.ConfigResponder;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/21/2016
 * Time: 2:25 PM
 */

public class ConfigServlet extends AbstractServlet {
    private static final Logger LOG = Logger.getLogger(ConfigServlet.class.getName());

    public ConfigServlet() {
        super();
        setResponder(new ConfigResponder());
    }

    @Override
    protected String getContentType() {
        return "application/json";
    }
}
