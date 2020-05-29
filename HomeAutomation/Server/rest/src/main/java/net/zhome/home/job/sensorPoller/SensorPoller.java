package net.zhome.home.job.sensorPoller;

import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.util.ZLogger;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class SensorPoller {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    private boolean stop = false;

    private SensorHost sensorHost;
    private SensorHostInterface sensorHostInterface;
    private SampleRepository sampleRepository;

    void setSampleRepository(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    void setSensorHostInterface(SensorHostInterface sensorHostInterface) {
        this.sensorHostInterface = sensorHostInterface;
    }


    private Runnable runnable = () -> {
        while (!stop) {
            try {
                Thread.sleep(1000 * sensorHost.getIntervalS());

                loop(sensorHost);
            } catch (InterruptedException e) {
                log.error("Sensor poller thread interrupted: " + sensorHost.getDescription());
                setStop();
            } catch (Throwable t) {
                log.error("Exception: ", t);
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    };
    private Thread thread = new Thread(runnable);


    SensorPoller(SensorHost sensorHost, SensorHostInterface sensorHostInterface, SampleRepository sampleRepository) {
        this.sensorHost = sensorHost;
        this.sensorHostInterface = sensorHostInterface;
        this.sampleRepository = sampleRepository;
    }

    void setStop() {
        stop = true;
    }

    void start() {
        thread.start();
    }

    boolean isAlive() {
        return thread.isAlive();
    }

    String getName() {
        return sensorHost.getDescription();
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

            sampleRepository.save(sample);
        }
    }

    @Override
    public String toString() {
        return "SensorPoller sensorHost id = " + sensorHost.getId();
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
