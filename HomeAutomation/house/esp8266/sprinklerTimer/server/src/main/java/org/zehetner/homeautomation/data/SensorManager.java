package org.zehetner.homeautomation.data;

import org.apache.log4j.Logger;
import org.zehetner.homeautomation.common.Sensor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/14/2016
 * Time: 7:31 PM
 */
public class SensorManager {
    private static final Logger LOG = Logger.getLogger(SensorManager.class.getName());

    private Map<String, Sensor> sensors;

    public void init() {
        LOG.info("in SensorManager.init()");
        initSensorSet();
    }

    public void deInit() {
        LOG.info("in SensorManager.deInit()");
        for (Sensor s : sensors.values()) {
            s.interrupt();
        }
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    private void initSensorSet() {
        sensors = new HashMap<>();

        Sensor s = new SHT2xSensor  ("sensor1", "Sensor 1", "http://sensor1.zhome.net/data");
        sensors.put(s.getSensorId(), s);
        s = new SHT2xSensor         ("sensor2", "Sensor 2", "http://sensor2.zhome.net/data");
        sensors.put(s.getSensorId(), s);
        s = new SHT2xSensor         ("sensor3", "Sensor 3", "http://sensor3.zhome.net/data");
        sensors.put(s.getSensorId(), s);
//        s = new SprinklerSensor     ("sprinklers", "Sprinklers", "sprinklers.zhome.net/data");
//        sensors.put(s.getSensorId(), s);

        for (Sensor sensor : sensors.values()) {
            if (sensor.init()) {
                sensor.start();
            }
        }
    }
}
