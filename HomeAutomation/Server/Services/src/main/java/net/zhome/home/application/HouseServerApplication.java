package net.zhome.home.application;

import net.zhome.home.job.sensorPoller.SensorPollerManager;
import net.zhome.home.util.ZLogger;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Map;
import java.util.Set;

@ApplicationPath("/resource")
public class HouseServerApplication extends Application {
    private final ZLogger log = ZLogger.getLogger(this.getClass());

    @Inject
    private SensorPollerManager sensorPollerManager;

    public HouseServerApplication() {
        super();
        createDirectories();
    }

    @Override
    public Map<String, Object> getProperties() {
        return super.getProperties();
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> singletons = super.getSingletons();

        sensorPollerManager.start();

        return singletons;
    }

    private void createDirectories() {
        try {
            java.io.File logDir = new java.io.File("/var/HouseServer/log");
            logDir.mkdirs();
        }
        catch (Throwable t) {
            ZLogger log = ZLogger.getLogger(HouseServerApplication.class);
            log.error("Failed to create log directory", t);
        }
    }

}

