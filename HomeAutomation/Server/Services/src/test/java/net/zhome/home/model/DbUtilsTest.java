package net.zhome.home.model;

import net.zhome.home.application.HouseServerApplication;
import net.zhome.home.persistence.model.Sample;
import net.zhome.home.persistence.model.Sensor;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.persistence.repository.SampleRepository;
import net.zhome.home.persistence.repository.SensorHostRepository;
import net.zhome.home.persistence.repository.SensorRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
@Component
@RunWith(SpringRunner.class)
@SpringBootTest(classes= HouseServerApplication.class)
@ActiveProfiles(profiles = "production")
public class DbUtilsTest {

    @Autowired
    private SensorHostRepository sensorHostRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SampleRepository sampleRepository;

    public DbUtilsTest() {
    }

    @Test
    public void eraseAndInitDb() {   //   ERASES the db
        eraseTheDb();
        addSensors();
    }

    @Test
    public void addDemoData() {
        long sampleCount = 100;
        long existingCount = sampleRepository.count();
        List<Sensor> sensors = sensorRepository.findAll();
        for (Sensor sensor : sensors) {
            for (int i=0; i<sampleCount; i++) {
                Sample sample = new Sample();
                sample.setSensorId(sensor.getId());
                sample.setTimeMs(new Date().getTime() + (i * 60 * 1000));
                float value = 100.0F * (float)Math.random();
                sample.setSensedValue(value);
                sampleRepository.save(sample);
            }
        }
        assertTrue((sampleCount * sensors.size() + existingCount) <= sampleRepository.count());
    }


    private void eraseTheDb() {
        sensorHostRepository.deleteAll();
        sensorRepository.deleteAll();
        sampleRepository.deleteAll();

        assertEquals(0, sensorHostRepository.findAll().size());
        assertEquals(0, sensorRepository.findAll().size());
        assertEquals(0, sampleRepository.count());

    }

    private void addSensors() {
        SensorHost sensorHost;

        sensorHost = addSensorHost("ESP8266", "http://sensor1.zhome.net", "Front Room");
            addSensor(sensorHost, "temperature", Sensor.Unit.DEG_C, "Temperature", "Temp");
            addSensor(sensorHost, "humidity", Sensor.Unit.PCT, "Humidity", "Hum");
            addSensor(sensorHost, "baro", Sensor.Unit.HPA, "Barometer", "Baro");

        sensorHost = addSensorHost("BME280", "http://sensor2.zhome.net", "Outside");
            addSensor(sensorHost, "temperature", Sensor.Unit.DEG_C, "Temperature", "Temp");
            addSensor(sensorHost, "humidity", Sensor.Unit.PCT, "Humidity", "Hum");
            addSensor(sensorHost, "baro", Sensor.Unit.HPA, "Barometer", "Baro");

        sensorHost = addSensorHost("BME280", "http://sensor3.zhome.net", "Attic");
            addSensor(sensorHost, "temperature", Sensor.Unit.DEG_C, "Temperature", "Temp");
            addSensor(sensorHost, "humidity", Sensor.Unit.PCT, "Humidity", "Hum");
            addSensor(sensorHost, "baro", Sensor.Unit.HPA, "Barometer", "Baro");

        sensorHost = addSensorHost("BME280", "http://sensor4.zhome.net", "Upstairs");
            addSensor(sensorHost, "temperature", Sensor.Unit.DEG_C, "Temperature", "Temp");
            addSensor(sensorHost, "humidity", Sensor.Unit.PCT, "Humidity", "Hum");
            addSensor(sensorHost, "baro", Sensor.Unit.HPA, "Barometer", "Baro");
    }

    private SensorHost addSensorHost(String description, String url, String location) {
        SensorHost sensorHost = new SensorHost();
        sensorHost.setActive(true);
        sensorHost.setDataUrl(url + "/data");
        sensorHost.setHomeUrl(url);
        sensorHost.setIntervalS(60L);
        sensorHost.setLocation(location);
        sensorHost.setDescription(description);
        sensorHostRepository.save(sensorHost);

        assertTrue(sensorHost.getId() != null);

        return sensorHost;
    }

    private void addSensor(SensorHost sensorHost, String propertyName, Sensor.Unit unit, String longName, String shortName) {
        Sensor sensor = new Sensor();
        sensor.setSensorHost(sensorHost);
        sensor.setPropertyName(propertyName);
        sensor.setUnit(unit);
        sensor.setLongName(longName);
        sensor.setShortName(shortName);
        sensor.setSensorType(Sensor.SensorType.SENSOR);
        sensorRepository.save(sensor);

        assertTrue(sensor.getId() != null);
    }
}
