package org.zehetner.homeautomation.common;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.zehetner.homeautomation.Worker;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/14/2016
 * Time: 5:40 PM
 */
public abstract class Sensor extends Thread implements Worker {
    private static final Logger LOG = Logger.getLogger(Sensor.class.getName());
    private long loopIntervalS = 20;
    private String sensorId;
    private String sensorName;

    public Sensor(String id, String name) {
        sensorId = id;
        sensorName = name;
    }

    public abstract boolean init();

    public abstract void measureAndLog() throws IOException;

    public abstract JSONArray getData(long startSeconds, long endSeconds);

    public void junitSetLoopDelay(final long delayS) {
        loopIntervalS = delayS;
    }

    public void run() {
        boolean keepLooping = true;
        long lastTime = System.currentTimeMillis();
        do {
            LOG.debug("At the top of the Sensor thread loop.");
            try {
                long now = System.currentTimeMillis();
                long remainingTime = lastTime + loopIntervalS*1000 - now;
                if (remainingTime > 0) {
                    Thread.sleep(remainingTime + 1);
                } else {
                    lastTime = now;
                    this.measureAndLog();
                }
            } catch (InterruptedException e) {
               LOG.info("DataCollector thread normal exit");
               keepLooping = false;
            } catch (Throwable t) {
               LOG.warn("Unknown exception in Sensor: " + t.toString(), t);
            }
        } while (keepLooping);
    }

    public String getSensorId() {
        return sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }
}
