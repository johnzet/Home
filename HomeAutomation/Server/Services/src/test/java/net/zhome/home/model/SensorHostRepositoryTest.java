package net.zhome.home.model;

import net.zhome.home.application.HouseServerApplication;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@Component
@RunWith(SpringRunner.class)
@SpringBootTest(classes= HouseServerApplication.class)
public class SensorHostRepositoryTest {

    @Autowired
    private
    SensorRepository sensorRepo;

    @Autowired
    private
    SensorHostRepository sensorHostRepository;

    @Test
    public void sensorHostTest() {
        SensorHost h1 = new SensorHost();
        initSensorHost(h1);
        SensorHost h2 = new SensorHost();
        initSensorHost(h2);
        sensorHostRepository.saveAndFlush(h1);
        sensorHostRepository.saveAndFlush(h2);

        Sensor s1 = new Sensor();
        initSensor(s1);
        Sensor s2 = new Sensor();
        initSensor(s2);
        s1.setSensorHost(h1);
        s2.setSensorHost(h1);
        sensorRepo.saveAndFlush(s1);
        sensorRepo.saveAndFlush(s2);

        SensorHost hq1  = sensorHostRepository.findOne(h1.getId());
        SensorHost hq2  = sensorHostRepository.findOne(h2.getId());
        assertEquals(2, hq1.getSensors().size());
        assertEquals(0, hq2.getSensors().size());

        sensorHostRepository.deleteAll();
        assertEquals(0, sensorRepo.findAll().size());
    }

    @Test
    public void activeHostTest() {
        SensorHost h1 = new SensorHost();
        initSensorHost(h1);
        SensorHost h2 = new SensorHost();
        initSensorHost(h2);
        h2.setActive(false);
        sensorHostRepository.saveAndFlush(h1);
        sensorHostRepository.saveAndFlush(h2);

        List<SensorHost> hosts = sensorHostRepository.findByActive(true);
        assertEquals(1, hosts.size());
        assertEquals(h1.getId(), hosts.get(0).getId());

        sensorHostRepository.deleteAll();
    }

    @Test
    public void testOneToMany() {
        SensorHost h1 = new SensorHost();
        initSensorHost(h1);
        sensorHostRepository.saveAndFlush(h1);

        Sensor s1 = new Sensor();
        initSensor(s1);
        s1.setSensorHost(h1);
        sensorRepo.saveAndFlush(s1);

        assertEquals(1, h1.getSensors().size());
        assertEquals(h1, s1.getSensorHost());
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
}