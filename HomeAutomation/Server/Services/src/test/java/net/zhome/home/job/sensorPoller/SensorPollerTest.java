package net.zhome.home.job.sensorPoller;

import net.zhome.home.application.HouseServerApplication;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Component
@RunWith(SpringRunner.class)
@SpringBootTest(classes= HouseServerApplication.class)
public class SensorPollerTest {
    @Autowired
    private
    SensorHostRepository sensorHostRepo;

    @Autowired
    private
    SensorRepository sensorRepo;

    @Autowired
    private
    SampleRepository sampleRepo;

    @Test
    public void testNormal() {
        sampleRepo.deleteAll();

        SensorHost host = new SensorHost();
        initSensorHost(host);
        sensorHostRepo.saveAndFlush(host);
        SensorPoller poller = new SensorPoller("Poller", host.getId());
        poller.setSampleRepository(sampleRepo);
        poller.setSensorHostInterface(new MockSensorHostInterfaceImpl());

        Sensor sensor = new Sensor();
        initSensor(sensor);
        sensor.setSensorHost(host);
        sensor.setUnit(Sensor.Unit.DEG_C);
        sensor.setPropertyName("temperature");
        sensorRepo.saveAndFlush(sensor);

        sensor = new Sensor();
        initSensor(sensor);
        sensor.setSensorHost(host);
        sensor.setUnit(Sensor.Unit.PCT);
        sensor.setPropertyName("humidity");
        sensorRepo.saveAndFlush(sensor);

//        host = sensorHostRepo.findOne(host.getId());
        poller.loop(host);

        List<Sample> samples = sampleRepo.findAll();

        assertEquals(2, samples.size());

        sensorHostRepo.deleteAll();
        sensorRepo.deleteAll();
        sampleRepo.deleteAll();
    }

    private void initSensorHost(SensorHost sensorHost) {
        sensorHost.setDataUrl("");
        sensorHost.setHomeUrl("");
        sensorHost.setLocation("");
        sensorHost.setActive(true);
        sensorHost.setIntervalS(10L);
    }

    private void initSensor(Sensor sensor) {
        sensor.setLongName("");
        sensor.setShortName("");
        sensor.setPropertyName("");
        sensor.setSensorType(Sensor.SensorType.SENSOR);
        sensor.setUnit(Sensor.Unit.DEG_C);
    }

    private class MockSensorHostInterfaceImpl implements SensorHostInterface {

        @Override
        public Map<String, Object> getSensorData(SensorHost sensorHost) {
            Map<String, Object> returnJson = new HashMap<>();
            returnJson.put("temperature", 12.3F);
            returnJson.put("humidity", 42.1F);
            returnJson.put("garbage", 100.0F);
            return returnJson;
        }
    }

}