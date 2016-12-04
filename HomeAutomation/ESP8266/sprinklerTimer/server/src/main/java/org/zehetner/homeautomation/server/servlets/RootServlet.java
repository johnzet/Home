package org.zehetner.homeautomation.server.servlets;

import org.zehetner.homeautomation.server.responder.RootPageResponder;

public class RootServlet extends AbstractServlet {
	private static final long serialVersionUID = 1274082660215786165L;

	public RootServlet() {
		super();
		setResponder(new RootPageResponder());
	}
}
