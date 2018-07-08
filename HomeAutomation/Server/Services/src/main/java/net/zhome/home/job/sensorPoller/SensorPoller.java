package net.zhome.home.job.sensorPoller;

import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import net.zhome.home.util.ZLogger;

import javax.ejb.Local;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Local
@ApplicationScoped
public class SensorPoller extends Thread {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    private long sensorHostId;
    private boolean stop = false;

    private SensorHostInterface sensorHostInterface;
    private SampleRepository sampleRepository;

    @Inject
    private SensorHostRepository sensorHostRepository;

    @Inject
    private SensorRepository sensorRepository;



    @Inject
    void setSensorHostInterface(SensorHostInterface sensorHostInterface) {
        this.sensorHostInterface = sensorHostInterface;
    }

    @Inject
    void setSampleRepository(SampleRepository sampleRepo) {
        this.sampleRepository = sampleRepo;
    }


    public SensorPoller() {}

    SensorPoller(String name, long sensorHostId) {
        this.sensorHostId = sensorHostId;
    }


    void setStop() {
        stop = true;
    }

    @Override
    public void run() {
        while(!stop) {
            try {
                SensorHost sensorHost = sensorHostRepository.findOne(sensorHostId);
                Thread.sleep(1000 * sensorHost.getIntervalS());

                loop(sensorHost);
            }
            catch (InterruptedException e) {
                log.error("Sensor poller thread interrupted: " + getName());
                setStop();
            }
            catch (Throwable t) {
                log.error("Exception: ", t);
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    void loop(SensorHost sensorHost) {
        Map<String, Object> sensorData = sensorHostInterface.getSensorData(sensorHost);
        updateDatabase(sensorHost, sensorData);
    }

    private void updateDatabase(SensorHost sensorHost, Map<String, Object> sensorData) {
        if (sensorData == null) return;
        List<Sensor> sensors = sensorHost.getSensors();
        for (Sensor sensor : sensors) {
            String propName = sensor.getPropertyName();
            float value = ((Number)sensorData.get(propName)).floatValue();

            long nowMs = new Date().getTime();
            Sample sample = new Sample();
            sample.setSensorId(sensor.getId());
            sample.setTimeMs(nowMs);
            sample.setSensedValue(value);

            sampleRepository.saveAndFlush(sample);
        }
    }

    @Override
    public String toString() {
        return "SensorPoller sensorHost id = " + sensorHostId;
    }

    // TODO
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
