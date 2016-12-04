package org.zehetner.homeautomation.common;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.zehetner.homeautomation.data.SensorManager;
import org.zehetner.homeautomation.sprinklers.SprinklerMechanical;
import org.zehetner.homeautomation.sprinklers.SprinklerStateEngine;
import org.zehetner.homeautomation.stateengine.ProgramSet;


/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/26/11
 * Time: 10:54 AM
 */
public class Manager {
    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private static Manager singleton = null;
    private static DateTime junitDate = null;

    private final SprinklerMechanical sprinklerMechanical;
    private final SprinklerStateEngine sprinklerStateEngine;
    private final CombinedProperties properties;
    private final ProgramSet programSet;
    private final SensorManager dataCollector;

    private Manager() {
        throw new IllegalArgumentException("wrong ctor");
    }

    private Manager(final SprinklerMechanical sprinklerMechanicalArg,
                    final SprinklerStateEngine sprinklerStateEngineArg,
                    final CombinedProperties propertiesArg,
                    final ProgramSet programSetArg,
                    final SensorManager dc) {
        this.sprinklerMechanical = sprinklerMechanicalArg;
        this.sprinklerStateEngine = sprinklerStateEngineArg;
        this.properties = propertiesArg;
        this.programSet = programSetArg;
        this.dataCollector = dc;
    }

    private static synchronized Manager createManager() {
        final CombinedProperties properties = CombinedProperties.getSingleton();

        final SprinklerMechanical sprinklerMechanical = new SprinklerMechanical();
        final SprinklerStateEngine sprinklerStateEngine = new SprinklerStateEngine(sprinklerMechanical);
        final ProgramSet programSet = new ProgramSet();
        final SensorManager dataCollector = new SensorManager();

        return new Manager(sprinklerMechanical, sprinklerStateEngine,
                 properties, programSet, dataCollector);
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

    public CombinedProperties getProperties() {
        return this.properties;
    }

    public ProgramSet getProgramSet() {
        return this.programSet;
    }

    public SensorManager getDataCollector() {
        return this.dataCollector;
    }

    public static DateTime getDateNow() {
        if (junitDate != null) {
            return new DateTime(junitDate);
        }
        return new DateTime();
    }

    public static void junitSetDateNow(final DateTime now) {
        if (now == null) {
            junitDate = null;
        } else {
            junitDate = new DateTime(now);
        }
    }

    public void startThreads() {
        LOG.info("in Manager.startThreads()");
        try {
            this.sprinklerStateEngine.start();
        }
        catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }

        try {
            this.dataCollector.init();
        }
        catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        }
    }

    public void destroy() {
        LOG.info("in Manager.destroy()");
        this.dataCollector.deInit();
        this.sprinklerStateEngine.interrupt();
    }

    public static void junitAdjustDateNow(final long time) {
        if (junitDate == null) {
            junitDate = Manager.getDateNow();
        }
        junitSetDateNow(new DateTime(junitDate.plus(time)));
    }
}
