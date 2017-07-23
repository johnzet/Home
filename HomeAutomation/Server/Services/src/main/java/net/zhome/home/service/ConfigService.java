package net.zhome.home.service;

import net.zhome.home.util.ZLogger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@RestController
//@RequestMapping("/config")
public class ConfigService {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    public ConfigService() {
    }

    @RequestMapping(value = "/config", method = RequestMethod.GET, produces = "application/json")
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
