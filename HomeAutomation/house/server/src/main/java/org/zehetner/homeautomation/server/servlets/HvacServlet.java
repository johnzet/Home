package org.zehetner.homeautomation.server.servlets;

import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.server.responder.HvacPageResponder;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class HvacServlet  extends AbstractServlet {
	private static final long serialVersionUID = -4971609647308737269L;

    public HvacServlet() {
		super();
		setResponder(new HvacPageResponder());
	}

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        Manager.getSingleton();

    }
}
