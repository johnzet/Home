package org.zehetner.homeautomation.server.servlets;


/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/20/11
 * Time: 9:01 PM
 */
public class AbstractAjaxServlet extends AbstractServlet {
    private static final long serialVersionUID = 81599465129420960L;

    @Override
    protected String getContentType() {
        return "application/json";
    }
}
