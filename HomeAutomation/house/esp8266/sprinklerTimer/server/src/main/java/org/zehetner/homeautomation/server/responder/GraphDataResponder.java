package org.zehetner.homeautomation.server.responder;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zehetner.homeautomation.common.Manager;
import org.zehetner.homeautomation.common.Sensor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 5/21/2016
 * Time: 6:22 PM
 */
public class GraphDataResponder implements PageResponder {
    private static final Logger LOG = Logger.getLogger(GraphDataResponder.class.getName());
    public static final String SENSOR_ID = "sensorId";
    public static final String SENSOR_NAME = "sensorName";
    public static final String METADATA = "metadata";
    public static final String DATA = "data";
    public static final String ALL = "all";

    @Override
    public String respond(final HttpServletRequest request) throws IOException, JSONException {

//        String contextPath = request.getContextPath().substring(1);  //  house
//        String servletPath = request.getServletPath().substring(1);  //  graphdata
        String path =        (request.getPathInfo() == null)? ALL : request.getPathInfo().substring(1);
//        String sensorId = path.substring(0, (path.contains("/"))? path.indexOf("/") : path.length());

        Map<String, Sensor> sensors = new HashMap<>();
        if (ALL.equals(path)) {
            sensors.putAll(Manager.getSingleton().getDataCollector().getSensors());
        } else {
            sensors.put(path, Manager.getSingleton().getDataCollector().getSensor(path));
        }

        JSONArray jsonSensorArray = new JSONArray();
        for (Sensor sensor : sensors.values()) {
            if (sensor == null) continue;

            JSONObject jsonSensor = new JSONObject();
            JSONObject metadata = new JSONObject();
            metadata.put(SENSOR_ID, sensor.getSensorId());
            metadata.put(SENSOR_NAME, sensor.getSensorName());
            jsonSensor.put(METADATA, metadata);

            try {
                jsonSensor.put(DATA, sensor.getData());
            } catch (Exception e) {
                LOG.error(e);
            }
            jsonSensorArray.put(jsonSensor);
        }
        return "var graphData = " + jsonSensorArray.toString(2) + ";";
    }
}
