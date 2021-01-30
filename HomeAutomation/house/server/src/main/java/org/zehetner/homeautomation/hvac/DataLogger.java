package org.zehetner.homeautomation.hvac;

import org.apache.log4j.Logger;
import org.rrd4j.DsType;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.rrd4j.core.Util;
import org.zehetner.homeautomation.Worker;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.health.HealthMonitor;
import org.zehetner.homeautomation.sprinklers.SprinklerMechanical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.rrd4j.ConsolFun.AVERAGE;
import static org.rrd4j.ConsolFun.MAX;
import static org.rrd4j.ConsolFun.MIN;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 12/26/11
 * Time: 1:08 PM
 */
public class DataLogger implements Worker {
    private static final Logger LOG = Logger.getLogger(DataLogger.class);
    public static final String IN_TEMP_F = "InTempF";
    public static final String IN_HUMIDITY = "InHumidity";
    public static final String OUT_TEMP_F = "OutTempF";
    public static final String OUT_HUMIDITY = "OutHumidity";
    public static final String BAROMETER = "Barometer";
    public static final String SPRINKLERS = "Sprinklers";
    public static final String HEAT = "Heat";
    public static final String COOL = "Cool";
    public static final String FAN = "Fan";
    public static final String REPORTED_ERRORS = "Reported Errors";
    public static final String THERM1_BAT = "Thermostat 1 Battery";
    private RrdDb rrdDb = null;
    private Thread appenderThread = null;


    public DataLogger() {
        this.rrdDb = createDataLoggingDb();
    }

    public String getDataLoggerDbFileName() {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String subDir = "hvac";
        if (! new File(tmpDir + File.separator + subDir).isDirectory()) {
            new File(tmpDir + File.separator + subDir).mkdir();
        }
        return  tmpDir + File.separator + subDir + File.separator + "rrd4j.rrd";
    }

    private void assembleDataLoggingSample() throws IOException {
        final Sensors sensors = Manager.getSingleton().getSensors();
        final SprinklerMechanical sprinklers = Manager.getSingleton().getSprinklerMechanical();
        final HvacMechanical hvac = Manager.getSingleton().getHvacSystem().getHvacStateEngine().getHvacMechanical();
        final HealthMonitor hm = Manager.getSingleton().getHealthMonitor();


        final Sample sample;
        try {
            sample = this.rrdDb.createSample();
        } catch (IOException e) {
            LOG.error(e);
            return;
        }
        sample.setTime(Util.getTimestamp());
        sample.setValue(IN_TEMP_F, sensors.getIndoorTemperature().getFahrenheitTemperature());
        sample.setValue(IN_HUMIDITY, sensors.getIndoorHumidity());
        sample.setValue(OUT_TEMP_F, sensors.getOutdoorTemperature().getFahrenheitTemperature());
        sample.setValue(OUT_HUMIDITY, sensors.getOutdoorHumidity());
        sample.setValue(BAROMETER, sensors.getBarometer());
        sample.setValue(SPRINKLERS, (double)sprinklers.getActiveZone().getPhysicalRelayNumber());
        sample.setValue(HEAT, ((hvac.isOn(Equipment.HEAT_1))?1.0:0.0) + ((hvac.isOn(Equipment.HEAT_2))?2.0:0.0));
        sample.setValue(COOL, ((hvac.isOn(Equipment.COOL_1))?1.0:0.0) + ((hvac.isOn(Equipment.COOL_2))?2.0:0.0));
        sample.setValue(FAN, ((hvac.isOn(Equipment.FAN))?0.5:0.0));
        sample.setValue(REPORTED_ERRORS, hm.getGatewayCommErrors() + hm.getGatewaySensorErrors() + hm.getGatewayTimeoutErrors()
                + hm.getGatewayWdtResetCount() + hm.getThermostat1CommErrors() + hm.getThermostat1SensorErrors()
                + hm.getThermostat1TimeoutErrors() + hm.getThermostat1WdtResetCount());
        sample.setValue(THERM1_BAT, sensors.getThermostat1BatPercent());
        sample.update();

//        rrdDb.close();

    }

    private RrdDb createDataLoggingDb() {

        final String fileName = this.getDataLoggerDbFileName();
        RrdDb db;

        // attempt to open an existing db
        try {
            db = new RrdDb(fileName, false /*readonly*/);
            return db;
        } catch (FileNotFoundException e) {
            // This is normal for the first run - fall-through
        } catch (IOException e) {
            LOG.error(e);
            return null;
        }

        // first, define the RRD
        final RrdDef rrdDef = new RrdDef(fileName, 10L);
        rrdDef.addDatasource(IN_TEMP_F, DsType.GAUGE, 10L, 0.0, 100.0);
        rrdDef.addDatasource(IN_HUMIDITY, DsType.GAUGE, 10L, 0.0, 100.0);
        rrdDef.addDatasource(OUT_TEMP_F, DsType.GAUGE, 10L, -50.0, 150.0);
        rrdDef.addDatasource(OUT_HUMIDITY, DsType.GAUGE, 10L, 0.0, 100.0);
        rrdDef.addDatasource(BAROMETER, DsType.GAUGE, 10L, 0.0, 100.0);
        rrdDef.addDatasource(SPRINKLERS, DsType.GAUGE, 10L, 0.0, 8.0);
        rrdDef.addDatasource(HEAT, DsType.GAUGE, 10L, 0.0, 3.0);
        rrdDef.addDatasource(COOL, DsType.GAUGE, 10L, 0.0, 3.0);
        rrdDef.addDatasource(FAN, DsType.GAUGE, 10L, 0.0, 1.0);
        rrdDef.addDatasource(REPORTED_ERRORS, DsType.GAUGE, 10L, 0.0, 4000.0);
        rrdDef.addDatasource(THERM1_BAT, DsType.GAUGE, 10L, 0.0, 100.0);
        rrdDef.addArchive(AVERAGE, 0.5, 1, 6*60*24*14);
//        rrdDef.addArchive(AVERAGE, 0.5, 10, 6*60*24*14/10);
        rrdDef.addArchive(MAX, 0.5, 1, 6*60*24*14);
        rrdDef.addArchive(MIN, 0.5, 1, 6*60*24*14);

        try {
            db = new RrdDb(rrdDef);
        } catch (IOException e) {
            LOG.error(e);
            return null;
        }

        return db;
    }


    @Override
    public void start() {
        this.appenderThread = new Thread(new DataLogger.Appender(this));
        this.appenderThread.start();
    }

    @Override
    public void junitSetLoopDelay(final long delayMs) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() {
        this.appenderThread.interrupt();
    }

    private class Appender implements Runnable {
        private final DataLogger dataLogger;

        protected Appender(final DataLogger dataLoggerArg) {
            this.dataLogger = dataLoggerArg;
        }

        @Override
        public void run() {
            boolean keepLooping = true;
            do {
                try {
                    this.dataLogger.assembleDataLoggingSample();
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    LOG.info("DataLogger thread normal exit");
                    keepLooping = false;
                } catch (IOException e) {
                    LOG.warn("SensorPoller or data logger exception: ", e);
                } catch (Throwable t) {
                    LOG.warn("exception thrown in data logger: ", t);
                }
            } while (keepLooping);
        }
    }
}
