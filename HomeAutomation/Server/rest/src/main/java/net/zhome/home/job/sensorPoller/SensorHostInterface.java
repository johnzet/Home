package net.zhome.home.job.sensorPoller;


import net.zhome.home.persistence.model.SensorHost;

import java.util.Map;

public interface SensorHostInterface {

    Map<String, Object> getSensorData(SensorHost sensorHost);

}
