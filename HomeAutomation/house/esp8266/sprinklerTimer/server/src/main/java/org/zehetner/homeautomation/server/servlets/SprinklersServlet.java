package org.zehetner.homeautomation.server.servlets;

import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.server.responder.SprinklersPageResponder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/18/12
 * Time: 3:40 PM
 */
public class SprinklersServlet extends AbstractServlet {
    private static final long serialVersionUID = 2742367143524503585L;

    public SprinklersServlet() {
        super();
        setResponder(new SprinklersPageResponder());
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        Manager.getSingleton();

    }
}
