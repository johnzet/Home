package net.zhome.home.service;


import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.util.ZLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public ResponseEntity<List<SensorHost>> getSensorList() {
        List<SensorHost> sensorHosts = sensorHostRepository.findAll();

        return getResponseEntity(sensorHosts);
    }

    @RequestMapping(value = "/data/{sensorId}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<ConciseSensorData> getSensorData(@PathVariable("sensorId") long sensorId, @RequestParam("range") String range) {
        return getSensorDataCommon(sensorId, range);
    }

    @RequestMapping(value = "/data/current", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<Sample>> getCurrentSensorData() {
        return null; //getResponseEntity(sampleRepository.findCurrentSamples());
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

    private ResponseEntity<ConciseSensorData> getSensorDataCommon(long sensorId, String range) {
        List<Sample> samples;
        if (range == null || range.trim().length() == 0 || "all".equals(range.trim().toLowerCase())) {
            samples = sampleRepository.findBySensorIdOrderByTimeMsAsc(sensorId);
        } else if (range.contains(",")) {
            String [] times = range.split(",");
            samples = sampleRepository.findBySensorIdAndTimeMsGreaterThanEqualAndTimeMsLessThanEqualOrderByTimeMsAsc(sensorId, Long.parseLong(times[0]), Long.parseLong(times[1]));
        } else {
            samples = sampleRepository.findBySensorIdAndTimeMsGreaterThanEqualOrderByTimeMsAsc(sensorId, Long.parseLong(range));
        }

        ConciseSensorData data = new ConciseSensorData(samples);
        return getResponseEntity(data);
    }

    private <T> ResponseEntity<T> getResponseEntity(T entity) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<>(entity, headers, HttpStatus.OK);
    }

    private class ConciseSensorData {
        private Long sensorId;
        private List<ConciseSample> samples;

        ConciseSensorData(final List<Sample> samples) {
            this.samples = new ArrayList<>();
            if (samples == null || samples.size() <= 0) {
                this.sensorId = -1L;
            } else {
                this.sensorId = samples.get(0).getSensorId();
                for (Sample s : samples) {
                    this.samples.add(new ConciseSample(s));
                }
            }
        }

        public Long getSensorId() {
            return sensorId;
        }

        public List<ConciseSample> getSamples() {
            return samples;
        }
    }

    private class ConciseSample {
        private Long t;
        private Float v;

        ConciseSample(Sample s) {
            this.t = s.getTimeMs();
            this.v = s.getSensedValue();
        }

        public Long getT() {
            return t;
        }

        public Float getV() {
            return v;
        }
    }
}
