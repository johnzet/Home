package net.zhome.home.job.sensorPoller;

import net.zhome.home.application.HouseServerApplication;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SensorHostRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@Component
@RunWith(SpringRunner.class)
@SpringBootTest(classes= HouseServerApplication.class)
public class SensorPollerManagerTest {

    @Autowired
    private
    SensorHostRepository sensorHostRepo;

    @Test
    public void testEmptyDb() {
        SensorPollerManager mgr = new SensorPollerManager();
        mgr.setSensorHostRepository(sensorHostRepo);
        mgr.loop();
        assertEquals(0, mgr.getSensorPollerCount());
    }

    @Test
    public void testPollerCrud() {
        SensorPollerManager mgr = new SensorPollerManager();
        mgr.setSensorHostRepository(sensorHostRepo);

        SensorHost h1 = new SensorHost();
        initSensorHost(h1);
        sensorHostRepo.saveAndFlush(h1);

        SensorHost h2 = new SensorHost();
        initSensorHost(h2);
        sensorHostRepo.saveAndFlush(h2);

        mgr.loop();
        assertEquals(2, mgr.getSensorPollerCount());

        SensorHost h3 = new SensorHost();
        initSensorHost(h3);
        sensorHostRepo.saveAndFlush(h3);

        mgr.loop();
        assertEquals(3, mgr.getSensorPollerCount());

        h3.setActive(false);
        sensorHostRepo.saveAndFlush(h3);

        mgr.loop();
        assertEquals(2, mgr.getSensorPollerCount());

        sensorHostRepo.deleteAll();
    }

    private void initSensorHost(SensorHost sensorHost) {
        sensorHost.setDataUrl("");
        sensorHost.setHomeUrl("");
        sensorHost.setLocation("");
        sensorHost.setActive(true);
        sensorHost.setIntervalS(10L);
    }
}