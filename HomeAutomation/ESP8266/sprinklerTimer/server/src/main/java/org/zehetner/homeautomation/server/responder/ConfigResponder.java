package org.zehetner.homeautomation.server.responder;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.zehetner.homeautomation.common.Manager;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/21/2016
 * Time: 6:22 PM
 */
public class ConfigResponder implements PageResponder {
    private static final Logger LOG = Logger.getLogger(ConfigResponder.class.getName());

    @Override
    public String respond(final HttpServletRequest request) throws IOException, JSONException {

//        String contextPath = request.getContextPath().substring(1);  //  house
//        String servletPath = request.getServletPath().substring(1);  //  config

        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("timeUtc", Manager.getDateNow().getMillis()/1000);
        jsonResponse.put("timeLocal", Manager.getDateNow().getZone().convertUTCToLocal(Manager.getDateNow().getMillis())/1000);

        DateTime dt = Manager.getDateNow();
        String rtcShortcut =
            dt.getSecondOfMinute() + " " +
            dt.getMinuteOfHour() + " " +
            dt.getHourOfDay() + " " +
            dt.getDayOfMonth() + " " +
            dt.getMonthOfYear() + " " +
            dt.getDayOfWeek() + " " + // numerically, these just happen to align with the ARM RTC
            (dt.getYear()-2000)
        ;

        jsonResponse.put("rtcShortcut", rtcShortcut);

        return jsonResponse.toString(4);
    }
}
