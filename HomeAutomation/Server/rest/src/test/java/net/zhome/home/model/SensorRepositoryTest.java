package net.zhome.home.model;

import net.zhome.home.AbstractIntegrationTest;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import org.junit.Test;

import javax.inject.Inject;

import static junit.framework.TestCase.assertTrue;

public class SensorRepositoryTest extends AbstractIntegrationTest {

    @Inject
    private SampleRepository sampleRepo;

    @Inject
    private SensorRepository sensorRepo;

    @Inject
    private SensorHostRepository sensorHostRepository;

    @Test
    public void testBogus() {
        assertTrue(true);
    }

}