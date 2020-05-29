package net.zhome.home.job.sensorPoller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.zhome.home.persistence.model.SensorHost;
import net.zhome.home.util.ZLogger;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class SensorHostInterface {

    private ZLogger log = ZLogger.getLogger(this.getClass());

    public Map<String, Object> getSensorData(SensorHost sensorHost) {
        InputStream is = null;
        try {
            URL url = new URL(sensorHost.getDataUrl());
            is = url.openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonText, new TypeReference<Map<String,Object>>(){});
        } catch (IOException e ) {
            log.warn("Exception in getSensorData(): ", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn("Exception in getSensorData(): ", e);
                }
            }
        }
        return null;
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

}
