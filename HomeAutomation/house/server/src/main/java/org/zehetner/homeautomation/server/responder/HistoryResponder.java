package org.zehetner.homeautomation.server.responder;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.apache.log4j.Logger;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.Util;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.hvac.DataLogger;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/24/11
 * Time: 12:16 PM
 */
public class HistoryResponder implements PageResponder {
    private static final Logger LOG = Logger.getLogger(HistoryResponder.class);
    private static final String START_HOURS_AGO = "startHoursAgo";
    private static final String END_HOURS_AGO = "endHoursAgo";
    private static final long REFRESH_PERIOD = 5 * 60 * 1000;

    @Override
    public String respond(final HttpServletRequest request) {

        long startHoursAgo = 4;
        long endHoursAgo = 0;
        try {
            startHoursAgo =  Long.parseLong(request.getParameter(START_HOURS_AGO));
        } catch (NumberFormatException nfe) {
            startHoursAgo = 4;
        }
        try {
            endHoursAgo =  Long.parseLong(request.getParameter(END_HOURS_AGO));
        } catch (NumberFormatException nfe) {
            endHoursAgo = 0;
        }



        final StringBuilder sb = new StringBuilder(4000);
        sb.append("<html><head>");
        sb.append("<script type='text/javascript' src='javascript/core.js'></script>");
        sb.append("<script type='text/javascript' src='javascript/hvac.js'></script>");

        sb.append("</head>\n");
        sb.append("<body onload=\"document.getElementById('startHoursAgo').value = ");
        sb.append(startHoursAgo);
        sb.append("; document.getElementById('endHoursAgo').value = ");
        sb.append(endHoursAgo);
        sb.append("; periodicRefresh(");
        sb.append(REFRESH_PERIOD);
        sb.append("); \">");

        sb.append("View history from ");
        sb.append("<input type='text' id='startHoursAgo' size='4' value='4' />");
        sb.append(" hours ago to ");
        sb.append("<input type='text' id='endHoursAgo' size='4' value='0' />");
        sb.append(" hours ago.  ");
        sb.append("<input type='button' onclick='refreshHistory();' value='Load' />");
        sb.append("<p/>");

        RrdGraph graph = generateGraph(new String[] {DataLogger.IN_TEMP_F}, "Indoor Temperature", new Paint[] {Color.blue}, new String[] {"Indoor Temp °F"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(new String[] {DataLogger.IN_HUMIDITY}, "Indoor Humidity", new Paint[] {Color.green}, new String[] {"Indoor Humidity %RH"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(new String[] {DataLogger.OUT_TEMP_F}, "Outdoor Temperature", new Paint[] {Color.blue}, new String[] {"Outdoor Temp °F"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(new String[] {DataLogger.OUT_HUMIDITY}, "Outdoor Humidity", new Paint[] {Color.green}, new String[] {"Outdoor Humidity %RH"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(new String[] {DataLogger.BAROMETER}, "Barometer", new Paint[] {Color.CYAN}, new String[] {"Barometer inHg (5 Minute Average)"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(
                new String[] {DataLogger.SPRINKLERS, DataLogger.HEAT, DataLogger.COOL, DataLogger.FAN},
                "Relays",
                new Paint[] {Color.GREEN, Color.RED, Color.BLUE, Color.CYAN},
                new String[] {"Sprinklers", "Heat", "Cool", "Fan"},
                startHoursAgo,
                endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(new String[] {DataLogger.REPORTED_ERRORS}, "Reported Errors", new Paint[] {Color.RED}, new String[] {"Module-Reported Errors"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        graph = generateGraph(new String[] {DataLogger.THERM1_BAT}, "Remaining Battery", new Paint[] {Color.CYAN}, new String[] {"Thermostat 1 Battery level %"}, startHoursAgo, endHoursAgo);
        sb.append("<img src='data:image/png;base64,");
        sb.append(Base64.encode(graph.getRrdGraphInfo().getBytes()));
        sb.append("'/>");

        sb.append("</body></html>");

        return sb.toString();
    }

    private RrdGraph generateGraph(final String[] dsName, final String title, final Paint[] lineColor, final String[] lineLegend,
                                   final long startHoursAgo, final long endHoursAgo) {
        final String rrdFileName = Manager.getSingleton().getDataLogger().getDataLoggerDbFileName();
        final RrdGraphDef gDef = new RrdGraphDef();
        gDef.setImageFormat("PNG");
        gDef.setWidth(400);
        gDef.setHeight(200);
        gDef.setFilename("-");
        gDef.setStartTime(-startHoursAgo*3600L );
        gDef.setEndTime((endHoursAgo == 0L)? Util.getTimestamp("now") : -endHoursAgo*3600L);
        gDef.setTitle(title);
        gDef.setAltAutoscale(true);

        for (int i=0; i<dsName.length; i++) {
            gDef.datasource(dsName[i], rrdFileName, dsName[i], ConsolFun.AVERAGE);

            gDef.line(dsName[i], lineColor[i], lineLegend[i], 1.0f);
        }

        // then actually draw the graph
        RrdGraph graph = null; // will create the graph in the path specified
        try {
            graph = new RrdGraph(gDef);
        } catch (IOException e) {
            LOG.error("Couldn't create graph: ", e);
        }
        return graph;
    }
}
