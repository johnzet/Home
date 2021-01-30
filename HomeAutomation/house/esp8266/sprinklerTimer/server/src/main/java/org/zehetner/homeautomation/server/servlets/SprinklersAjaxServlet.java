package org.zehetner.homeautomation.server.servlets;

import org.zehetner.homeautomation.server.responder.SprinklersAjaxResponder;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/18/12
 * Time: 3:41 PM
 */
public class SprinklersAjaxServlet extends AbstractAjaxServlet {
    private static final long serialVersionUID = -4058624688654817688L;

    public SprinklersAjaxServlet() {
        super();
        setResponder(new SprinklersAjaxResponder());
    }

}
