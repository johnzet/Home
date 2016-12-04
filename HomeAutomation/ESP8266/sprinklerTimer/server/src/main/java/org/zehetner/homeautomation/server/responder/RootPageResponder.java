package org.zehetner.homeautomation.server.responder;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;

public class RootPageResponder implements PageResponder {
	private static final Logger LOG = Logger.getLogger(RootPageResponder.class.getName());

	@Override
 	public String respond(final HttpServletRequest request) throws IOException {

    	final StringBuilder rootHtml = new StringBuilder(4096);
		final String path = "root.html";

     	final InputStreamReader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(path));
		try {

			final char[] buf = new char[1024];

			int r;

			while ((r = reader.read(buf)) != -1) {
				rootHtml.append(buf, 0, r);
			}
		}
		finally {
			reader.close();
		}
		return rootHtml.toString();
}

}
