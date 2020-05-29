package net.zhome.home.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Calendar;
import java.util.Date;

@Path("/config")
@Consumes({ "application/json" })
@Produces({ "application/json" })

public class ConfigService {
//    private final ZLogger log = ZLogger.getLogger(this.getClass());

    public ConfigService() {
    }

    @GET
    @Path("/config")
    public String getSensorList() {

//        {
//            "rtcShortcut": "31 2 13 22 7 6 17",
//                "timeLocal": 1500728551,
//                "timeUtc": 1500750151
//        }

        final Date now = new Date();
        int tzOffset = Calendar.getInstance().getTimeZone().getRawOffset();
        Calendar cal = Calendar.getInstance();
        return "{\n" +
                "\"timeLocal\": " + (now.getTime() + tzOffset) / 1000 + ",\n" +
                "\"timeUtc\": " + now.getTime() / 1000 + ",\n" +
                "\"rtcShortcut\": \"" +
                cal.get(Calendar.SECOND) + " " +
                cal.get(Calendar.MINUTE) + " " +
                cal.get(Calendar.HOUR_OF_DAY) + " " +
                cal.get(Calendar.DAY_OF_MONTH) + " " +
                cal.get(Calendar.MONTH) + " " +
                (cal.get(Calendar.YEAR) - 2000) + "\"" +
                "}\n";
    }
}
