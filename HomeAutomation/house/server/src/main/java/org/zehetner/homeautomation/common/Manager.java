package org.zehetner.homeautomation.common;

import org.zehetner.homeautomation.health.HealthMonitor;
import org.zehetner.homeautomation.hvac.DataLogger;
import org.zehetner.homeautomation.hvac.HvacSystem;
import org.zehetner.homeautomation.hvac.SensorPoller;
import org.zehetner.homeautomation.hvac.Sensors;
import org.zehetner.homeautomation.sprinklers.SprinklerMechanical;
import org.zehetner.homeautomation.sprinklers.SprinklerStateEngine;
import org.zehetner.homeautomation.stateengine.ProgramSet;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/26/11
 * Time: 10:54 AM
 */
public class Manager {
    private static Manager singleton = null;
    private static Date junitDate = null;

    private final HvacSystem hvacSystem;
    private final SprinklerMechanical sprinklerMechanical;
    private final SprinklerStateEngine sprinklerStateEngine;
    private final Sensors sensors;
    private final SensorPoller sensorPoller;
    private final DataLogger dataLogger;
    private final CombinedProperties properties;
    private final ProgramSet programSet;
    private final HealthMonitor healthMonitor;

    private Manager() {
        throw new IllegalArgumentException("wrong ctor");
    }

    private Manager(final HvacSystem hvacSystemArg, final SprinklerMechanical sprinklerMechanicalArg,
                    final SprinklerStateEngine sprinklerStateEngineArg,
                    final Sensors sensorsArg, final SensorPoller sensorPollerArg,
                    final DataLogger dataLoggerArg, final CombinedProperties propertiesArg,
                    final ProgramSet programSetArg, final HealthMonitor healthMonitorArg) {
        this.hvacSystem = hvacSystemArg;
        this.sprinklerMechanical = sprinklerMechanicalArg;
        this.sprinklerStateEngine = sprinklerStateEngineArg;
        this.sensors = sensorsArg;
        this.sensorPoller = sensorPollerArg;
        this.dataLogger = dataLoggerArg;
        this.properties = propertiesArg;
        this.programSet = programSetArg;
        this.healthMonitor = healthMonitorArg;
    }

    private static synchronized Manager createManager() {
        final CombinedProperties properties = CombinedProperties.getSingleton();

        final HvacSystem hvacSystem = new HvacSystem();
        final Sensors sensors = new Sensors();
        final SprinklerMechanical sprinklerMechanical = new SprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = new SprinklerStateEngine(sprinklerMechanical);
        final SensorPoller sensorPoller = new SensorPoller();
        final DataLogger dataLogger = new DataLogger();
        final ProgramSet programSet = new ProgramSet();
        final HealthMonitor healthMonitor = new HealthMonitor();

        return new Manager(hvacSystem, sprinklerMechanical, sprinklerStateEngine,
                sensors, sensorPoller, dataLogger, properties, programSet, healthMonitor);
    }

    public static synchronized Manager getSingleton() {
        if (singleton == null) {
            singleton = createManager();
        }
        return singleton;
    }

    public static synchronized void junitDeleteManager() {
        singleton = null;
    }

    public SprinklerMechanical getSprinklerMechanical() {
        return this.sprinklerMechanical;
    }

    public SprinklerStateEngine getSprinklerStateEngine() {
        return this.sprinklerStateEngine;
    }

    public Sensors getSensors() {
        return this.sensors;
    }

    public HvacSystem getHvacSystem() {
        return this.hvacSystem;
    }

    public SensorPoller getSensorPoller() {
        return this.sensorPoller;
    }

    public DataLogger getDataLogger() {
        return this.dataLogger;
    }

    public HealthMonitor getHealthMonitor() {
        return this.healthMonitor;
    }

    public CombinedProperties getProperties() {
        return this.properties;
    }

    public ProgramSet getProgramSet() {
        return this.programSet;
    }

    public static Date getDateNow() {
        if (junitDate != null) {
            return new Date(junitDate.getTime());
        }
        return new Date();
    }

    public static void junitSetDateNow(final Date now) {
        if (now == null) {
            junitDate = null;
        } else {
            junitDate = new Date(now.getTime());
        }
    }

    public void startThreads() {
        this.hvacSystem.start();
        this.sprinklerStateEngine.start();
        this.dataLogger.start();
    }

    public void destroy() {
        this.hvacSystem.stop();
        this.sprinklerStateEngine.stop();
        this.dataLogger.stop();
    }

    public static void junitAdjustDateNow(final long time) {
        if (junitDate == null) {
            junitDate = Manager.getDateNow();
        }
        junitSetDateNow(new Date(junitDate.getTime() + time));
    }
}
