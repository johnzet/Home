package org.zehetner.homeautomation.data;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.zehetner.homeautomation.common.Sensor;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/14/2016
 * Time: 5:42 PM
 */
public abstract class JsonResponseSensor extends Sensor {
    private static final Logger LOG = Logger.getLogger(JsonResponseSensor.class.getName());

    private static final String TEMPERATURE_C = "temperature";
    private static final String HUMIDITY_PCT = "humidity";
    private static final String BAROMETER_HPA = "barometer";
    private static final String SPRINKLER_STATE = "state";
    protected long loopIntervalS;
    private String sensorUrl;

    public JsonResponseSensor(String sensorId, String sensorName, String url) {
        super(sensorId, sensorName);
        sensorUrl = url;
    }

    public String getSensorUrl() {
        return sensorUrl;
    }

    protected JSONObject getJsonResponse(String urlStr, String urlParameters) {

        JSONObject json;
        HttpURLConnection connection = null;
        try {
            LOG.info("Sending request to " + urlStr);
            //Create connection
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setConnectTimeout(8 * 1000);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            LOG.info("Got JSON response: " + response.toString());
            json = new JSONObject(response.toString());

            return json;
        } catch (SocketTimeoutException timeout) {
            LOG.warn("Socket read timeout for sensor " + getSensorName(), timeout);
        } catch (Exception e) {
            LOG.warn("Exception: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONObject();
    }


    public String getDataLoggerDbFileName() {
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String subDir = this.getSensorName();
        if (! new File(tmpDir + File.separator + subDir).isDirectory()) {
            new File(tmpDir + File.separator + subDir).mkdir();
        }
        return  tmpDir + File.separator + subDir + File.separator + "rrd4j.rrd";
    }

    @Override
    public void junitSetRunEnabled(final boolean enabled) {
    }

    protected long roundToInterval(double timeS) {
        return (Math.round(timeS / (double)loopIntervalS)) * loopIntervalS;
    }
}
