package org.zehetner.homeautomation.server.servlets;

import org.apache.log4j.Logger;
import org.zehetner.homeautomation.server.responder.HistoryResponder;


/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/24/11
 * Time: 12:11 PM
 */
public class HistoryServlet extends AbstractServlet {
    private static final Logger LOG = Logger.getLogger(HistoryServlet.class);

    private static final long serialVersionUID = -2859308993984083219L;

    public HistoryServlet() {
         setResponder(new HistoryResponder());
    }

//    @Override
//    public void startThreads(final ServletConfig common) throws ServletException {
//        super.startThreads(common);    //To change body of overridden methods use File | HvacSettings | File Templates.
//
//    }
}
