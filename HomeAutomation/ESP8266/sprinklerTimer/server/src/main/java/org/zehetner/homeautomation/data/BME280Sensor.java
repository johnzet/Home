package org.zehetner.homeautomation.data;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.FetchRequest;
import org.rrd4j.core.RrdDb;
import org.rrd4j.core.RrdDef;
import org.rrd4j.core.Sample;
import org.zehetner.homeautomation.common.Measurement;
import org.zehetner.homeautomation.common.Unit;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/14/2016
 * Time: 5:42 PM
 */
public class BME280Sensor extends JsonResponseSensor {
    private static final Logger LOG = Logger.getLogger(BME280Sensor.class.getName());

    private static final String TEMPERATURE_C = "temperature";
    private static final String HUMIDITY_PCT = "humidity";
    private static final String BAROMETER_HPA = "baro";
    private static final String TEMP_COL = "Temperature";
    private static final String HUM_COL = "Humidity";
    private static final String BAROM_COL = "Barometer";
    private long rows;


    public BME280Sensor(String sensorId, String sensorName, String url) {
        super(sensorId, sensorName, url);
    }

    public boolean init() {
        loopIntervalS = 20;
        long durationS = 60*60*24*7;
        rows = durationS / loopIntervalS;
        return createDataLoggingDb();
    }

    private boolean createDataLoggingDb() {

        final String fileName = this.getDataLoggerDbFileName();

        RrdDb rrdDb;
        try {
            rrdDb = new RrdDb(fileName);
            rrdDb.close();
            return true;
        } catch (IOException e) {
            /*normal if there is no db yet*/
        }

        // first, define the RRD
        RrdDef rrdDef = new RrdDef(fileName, loopIntervalS);
        rrdDef.setVersion(2);
        long startTime = roundToInterval(System.currentTimeMillis() / 1000);
        rrdDef.setStartTime(startTime);
        rrdDef.addDatasource(TEMP_COL, DsType.GAUGE, loopIntervalS, -100.0, 100.0);
        rrdDef.addDatasource(HUM_COL, DsType.GAUGE, loopIntervalS, 0.0, 100.0);
        rrdDef.addDatasource(BAROM_COL, DsType.GAUGE, loopIntervalS, 70, 110.0);
        rrdDef.addArchive(ConsolFun.AVERAGE, 0.5, 1, (int) rows);
//        rrdDef.addArchive(ConsolFun.MAX, 0.5, 1, (int)rows);
//        rrdDef.addArchive(ConsolFun.MIN, 0.5, 1, (int)rows);


        try {
            LOG.info("Estimated file size: " + rrdDef.getEstimatedSize());
            rrdDb = new RrdDb(rrdDef);
            LOG.info("== RRD file created.");
            if (rrdDb.getRrdDef().equals(rrdDef)) {
                LOG.info("Checking RRD file structure... OK");
            } else {
                LOG.info("Invalid RRD file created. This is a serious bug, bailing out");
                return false;
            }
            rrdDb.close();
            LOG.info("== RRD file closed.");
        } catch (IOException e) {
            LOG.warn(e);
            return false;
        }
        return true;
    }


    public void measureAndLog() throws IOException {
        Measurement temp = null;
        Measurement hum = null;
        Measurement barom = null;

        JSONObject json = this.getJsonResponse(this.getSensorUrl(), "");

        if (json.has(TEMPERATURE_C)) {
            double temperature = json.optDouble(TEMPERATURE_C);
            temp = new Measurement(temperature, Unit.DEGREES_C);
        }
        if (json.has(HUMIDITY_PCT)) {
            double humidity = json.optDouble(HUMIDITY_PCT);
            hum = new Measurement(humidity, Unit.PCT_RH);
        }
        if (json.has(BAROMETER_HPA)) {
            double b = json.optDouble(BAROMETER_HPA);
            barom = new Measurement(b, Unit.HPA);
        }
        assembleDataLoggingSample(temp, hum, barom);
    }


    private void assembleDataLoggingSample(final Measurement temp, final Measurement hum, final Measurement barom) throws IOException {

        final Sample sample;
        RrdDb rrdDb;
        rrdDb = new RrdDb(this.getDataLoggerDbFileName());
        try {
            sample = rrdDb.createSample();

            double systemSecs = System.currentTimeMillis() / 1000.0;
            long now = roundToInterval(systemSecs);
            LOG.info("Rounded seconds now = " + now);
            sample.setTime(now+1);
            sample.setValue(TEMP_COL, temp.getValue());
            sample.setValue(HUM_COL, hum.getValue());
            sample.setValue(BAROM_COL, barom.getValue());

            sample.update();

        } catch (IOException e) {
            LOG.error(e);
        }
        finally {
            rrdDb.close();
        }
    }

    public JSONArray getData(long startSeconds, long endSeconds) {
        RrdDb rrdDb;
        String dataStr;
        try {
            rrdDb = new RrdDb(this.getDataLoggerDbFileName());
            long startTime = (startSeconds>0? startSeconds : rrdDb.getLastUpdateTime() - (rows-1) * loopIntervalS);
            long endTime = (endSeconds>0? endSeconds : rrdDb.getLastUpdateTime() - loopIntervalS);
            FetchRequest fr = rrdDb.createFetchRequest(
                    ConsolFun.AVERAGE,
                    startTime,
                    endTime);
            dataStr = fr.fetchData().dump();

    //        LOG.info(rrdDb.dump());

            rrdDb.close();
        } catch (IOException e) {
            LOG.error("Couldn't get data from rrdb: ", e);
            dataStr = "";
        }

        StringReader stringReader = new StringReader(dataStr);
        LineNumberReader reader = new LineNumberReader(stringReader);

        JSONArray jsonArray = new JSONArray();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, ": \t");
                if (tok.countTokens() != 4) continue;
                String timeStr = tok.nextToken();
                String tempStr = tok.nextToken();
                String humStr = tok.nextToken();
                String baroStr = tok.nextToken();

                long timeStamp = Long.parseLong(timeStr);
                double temp = Double.parseDouble(tempStr);
                double hum = Double.parseDouble(humStr);
                double baro = Double.parseDouble(baroStr);
                if (!Double.isFinite(temp) || !Double.isFinite(hum) || !Double.isFinite(baro)) continue;

                JSONObject row = new JSONObject();
                row.put("timeS", timeStamp);
                row.put("tempC", temp);
                row.put("humPct", hum);
                row.put("baroHpa", baro);
                jsonArray.put(row);
            }
        } catch (IOException e) {
            LOG.error("Failed reading database dump", e);
        } catch (JSONException e) {
            LOG.error("Failed creating JSON db dump", e);
        }
        return jsonArray;
    }


}
