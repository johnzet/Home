package net.zhome.home.model;

import net.zhome.home.application.HouseServerApplication;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

@Component
@RunWith(SpringRunner.class)
@SpringBootTest(classes= HouseServerApplication.class)
public class SensorRepositoryTest {

    @Autowired
    private
    SampleRepository sampleRepo;

    @Autowired
    private
    SensorRepository sensorRepo;

    @Autowired
    private
    SensorHostRepository sensorHostRepository;

    @Test
    public void testBogus() {

    }

}