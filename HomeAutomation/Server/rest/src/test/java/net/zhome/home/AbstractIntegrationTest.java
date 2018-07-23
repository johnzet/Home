package net.zhome.home;

import net.zhome.home.job.sensorPoller.SensorHostInterface;
import net.zhome.home.persistence.model.AbstractEntity;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.AbstractRepository;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import org.h2.Driver;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * Created by John Zehetner on 7/22/18.
 */

@RunWith(Arquillian.class)
public class AbstractIntegrationTest {

    @Deployment
    public static Archive<?> createTestArchive() {

        Driver driver = org.h2.Driver.load();
        Archive<?> war = ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(AbstractEntity.class, Sample.class, Sensor.class, SensorHostInterface.class, SensorHost.class)
                .addClasses(SensorRepository.class, SampleRepository.class, SensorHostRepository.class)
                .addClasses(AbstractRepository.class, AbstractIntegrationTest.class)
                .addPackages(true, "org.postgresql")
                .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                ;
        return war;

    }


    public AbstractIntegrationTest() {

    }
}
