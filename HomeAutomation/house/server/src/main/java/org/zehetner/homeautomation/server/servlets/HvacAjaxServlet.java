package org.zehetner.homeautomation.server.servlets;

import org.zehetner.homeautomation.server.responder.HvacAjaxResponder;

public class HvacAjaxServlet  extends AbstractAjaxServlet {

	public HvacAjaxServlet() {
		super();
		setResponder(new HvacAjaxResponder());
	}
}
