package net.zhome.home.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.util.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/data/{sensorId}", method = RequestMethod.GET, produces = "application/json")
    public String getSensorData(@PathVariable("sensorId") long sensorId, @RequestParam("range") String range) {
        return getSensorDataCommon(sensorId, range);
    }

    @RequestMapping(value = "/data/current", method = RequestMethod.GET, produces = "application/json")
    public String getCurrentSensorData() {
        return getSensorDataCurrent();
    }

    @RequestMapping(value = "/data", method = RequestMethod.GET, produces = "text/html")
    public String getSensorDataUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("Usage: <br/><ul>");
        sb.append("<li>~/data (This usage statement)</li>");
        sb.append("<li>~/data/sensorId?range=&lt;time range&gt;</li>");
        sb.append("<li>~/data/current</li>");
        sb.append("<li>&nbsp;&nbsp;range = all | &lt;start ms&gt;,&lt;end ms&gt; | &lt;n milliseconds ago&gt;</li>");
        sb.append("</ul>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private String getSensorDataCommon(long sensorId, String range) {
        List<Sample> samples;
        if (range == null || range.trim().length() == 0 || "all".equals(range.trim().toLowerCase())) {
            samples = sampleRepository.findBySensorIdOrderByTimeMsAsc(sensorId);
        } else if (range.contains(",")) {
            String [] times = range.split(",");
            samples = sampleRepository.findBySensorIdAndTimeMsGreaterThanEqualAndTimeMsLessThanEqualOrderByTimeMsAsc(sensorId, Long.parseLong(times[0]), Long.parseLong(times[1]));
        } else {
            samples = sampleRepository.findBySensorIdAndTimeMsGreaterThanEqualOrderByTimeMsAsc(sensorId, Long.parseLong(range));
        }

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

    private String getSensorDataCurrent() {
        List<Sample> samples = sampleRepository.findByGreatestTimeMs();

        ObjectMapper mapper = new ObjectMapper();
        StringBuilder sb = new StringBuilder();
        Long timeNow = new Date().getTime();

        sb.append("[");
        boolean first = true;
        for (Sample sample : samples) {
            if (!first) {
                sb.append(",");
            } else {
                first = false;
            }

            if (Math.abs(timeNow - sample.getTimeMs()) < (10 * 60 * 1000)) {
                try {
                    sb.append(mapper.writeValueAsString(sample));
                } catch (JsonProcessingException e) {
                    log.error("json conversion exception", e);
                }
            }
        }
        sb.append("]");

        return sb.toString();
    }
}
