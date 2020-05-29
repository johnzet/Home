package net.zhome.home.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.util.ZLogger;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Date;
import java.util.List;

@Path("/sensor")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class SensorDataService {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    private SampleRepository sampleRepository;
    private SensorHostRepository sensorHostRepository;

    @Inject
    public SensorDataService(SampleRepository sampleRepository, SensorHostRepository sensorHostRepository) {
        this.sampleRepository = sampleRepository;
        this.sensorHostRepository = sensorHostRepository;
    }

    @GET
    @Path("/list")
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

    @GET
    @Path("/data/{sensorId}")
    public String getSensorData(@PathParam("sensorId") long sensorId, @QueryParam("range") String range) {
        return getSensorDataCommon(sensorId, range);
    }

    @GET
    @Path("/data/current")
    public String getCurrentSensorData() {
        return getSensorDataCurrent();
    }

    @GET
    @Produces({"test/html" })
    @Path("/data")
    public String getSensorDataUsage() {
        return "<html><body>" +
                "Usage: <br/><ul>" +
                "<li>~/data (This usage statement)</li>" +
                "<li>~/data/sensorId?range=&lt;time range&gt;</li>" +
                "<li>~/data/current</li>" +
                "<li>&nbsp;&nbsp;range = all | &lt;start ms&gt;,&lt;end ms&gt; | &lt;n milliseconds ago&gt;</li>" +
                "</ul>" +
                "</body></html>";
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
