package org.zehetner.homeautomation.server.servlets;

import org.zehetner.homeautomation.server.responder.PageResponder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class AbstractServlet extends HttpServlet {
	private static final long serialVersionUID = 1701950883525732877L;
	protected PageResponder responder = null;

	@Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}
	@Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	private void processRequest(final HttpServletRequest request,
			final HttpServletResponse response) throws IOException {
		try {
			response.setContentType(getContentType());
			response.setCharacterEncoding(getCharacterEncoding());
            response.addHeader("Cache-Control", "no-cache");
			response.getOutputStream().println(getResponder().respond(request));
		}
		catch (Throwable t) {
			response.getOutputStream().println("Error: " + t.toString());
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
	}

	protected void setResponder(final PageResponder responderArg) {
		this.responder = responderArg;
	}

    protected String getContentType() {
        return "text/html";
    }

    protected String getCharacterEncoding() {
        return "utf-8";
    }

	public PageResponder getResponder() {
		return this.responder;
	}

}
