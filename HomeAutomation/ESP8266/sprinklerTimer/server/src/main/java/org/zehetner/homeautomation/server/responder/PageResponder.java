package org.zehetner.homeautomation.server.responder;

import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public interface PageResponder {
	String respond(HttpServletRequest request) throws IOException, JSONException;
}
