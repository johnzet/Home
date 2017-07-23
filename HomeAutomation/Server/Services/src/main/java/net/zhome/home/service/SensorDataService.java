package net.zhome.home.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.util.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/sensor")
public class SensorDataService {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    private SampleRepository sampleRepository;
    private SensorHostRepository sensorHostRepository;

    public SensorDataService() {
    }

    @Autowired
    public SensorDataService(SampleRepository sampleRepository, SensorHostRepository sensorHostRepository) {
        this.sampleRepository = sampleRepository;
        this.sensorHostRepository = sensorHostRepository;
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = "application/json")
    public String getSensorList() {

        ObjectMapper mapper = new ObjectMapper();
        List<SensorHost> sensorHosts = sensorHostRepository.findAll();

        try {
            return mapper.writeValueAsString(sensorHosts);
        } catch (JsonProcessingException e) {
            log.error("json conversion exception", e);
        }
        return "{}";
    }

    @RequestMapping(value = "/data/{sensorId}/{startHours}", method = RequestMethod.GET, produces = "application/json")
    public String getSensorData(@PathVariable("sensorId") long sensorId, @PathVariable("startHours") long startHours) {
        return getSensorDataCommon(sensorId, startHours);
    }

    @RequestMapping(value = "/data/{sensorId}", method = RequestMethod.GET, produces = "application/json")
    public String getSensorData(@PathVariable("sensorId") long sensorId) {
        return getSensorDataCommon(sensorId, -1);
    }

    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = "text/html")
    public String getSensorDataUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("Usage: <br/><ul>");
        sb.append("<li>~/data (This usage statement)</li>");
        sb.append("<li>~/data/&lt;sensorId&gt; (All data for this sensor)</li>");
        sb.append("<li>~/data/&lt;sensorId&gt;/&lt;hours&gt; (Data for this sensor starting &lt;hours&gt; ago)</li>");
        sb.append("</ul>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String getSensorDataCommon(long sensorId, long hours) {
        ObjectMapper mapper = new ObjectMapper();
        long startMs = 0;
        if (hours > 0) {
            startMs = new Date().getTime() - (hours * 3600 * 1000);
        }
        List<Sample> samples = sampleRepository.findBySensorIdAndTimeMsGreaterThanEqual(sensorId, startMs);

        StringBuilder sb = new StringBuilder(4096);
        sb.append("{\"sensorId\": ").append(sensorId);
        sb.append(", \"data\": [");
        boolean first = true;
        for (Sample sample : samples) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }
            sb.append("[");
            sb.append(sample.getTimeMs());
            sb.append(",");
            sb.append(sample.getSensedValue());
            sb.append("]");
        }
        sb.append("]}");
        return sb.toString();
    }
}
