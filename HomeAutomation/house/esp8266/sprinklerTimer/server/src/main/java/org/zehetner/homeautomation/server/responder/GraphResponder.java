package org.zehetner.homeautomation.server.responder;

import org.apache.log4j.Logger;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/21/2016
 * Time: 6:22 PM
 */



public class GraphResponder implements PageResponder {
    private static final Logger LOG = Logger.getLogger(GraphResponder.class.getName());

    @Override
    public String respond(final HttpServletRequest request) throws IOException, JSONException {
        String contextPath = request.getContextPath();
        String sensorName = (request.getPathInfo()==null? "" : request.getPathInfo().split("/")[1]);
        if (sensorName == null || sensorName.length()<=0) sensorName = GraphDataResponder.ALL;
        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head lang=\"en\">\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width\">\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"/house/css/core.css\" />\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"" + contextPath + "/css/dc.css\" />\n" +
                "    <title></title>\n" +
                "    <script type=\"text/javascript\" src=\"" + contextPath + "/javascript/d3.js\"></script>\n" +
                "    <script type=\"text/javascript\" src=\"" + contextPath + "/javascript/crossfilter.js\"></script>\n" +
                "    <script type=\"text/javascript\" src=\"" + contextPath + "/javascript/dc.js\"></script>\n" +
                "    <script type=\"text/javascript\" src=\"" + contextPath + "/javascript/graph.js\"></script>\n" +
                "    <script type=\"text/javascript\" src=\"" + contextPath + "/graphdata/" + sensorName + "\"></script>\n" +
                "    <script type=\"text/javascript\">sensorName = '" + sensorName + "';</script>\n" +
                "</head>\n" +
                "<body onload=\"drawChart()\" >\n" +
                "<div id=\"chart\" style=\"display: block; width: 100%;\"></div>\n" +
                "<div id=\"other chart\" style=\"display: block;  width: 100%;\"></div>\n" +
                "<div id=\"stats\" style=\"display: block;\"></div>\n" +
                "</body>\n" +
                "</html>";

        return html;
    }
}
