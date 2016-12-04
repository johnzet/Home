package org.zehetner.homeautomation.sprinklers;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: johnz
 * Date: 3/12/2016
 * Time: 12:52 PM
 */
public class SprinklerHttpComm {
    private static final Logger LOG = Logger.getLogger(SprinklerHttpComm.class.getName());

    public String executeGet(final String targetURL, final String postRequestData) {
        HttpURLConnection connection = null;
        DataOutputStream wr = null;
        BufferedReader rd = null;
        try {
            //Create connection
            final URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type",
                    "application/json");

            if (postRequestData != null) {
                connection.setRequestProperty("Content-Length",
                        Integer.toString(postRequestData.getBytes().length));
            }
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            wr = new DataOutputStream(
                    connection.getOutputStream());
            if (postRequestData != null) wr.writeBytes(postRequestData);
            wr.flush();

            //Get Response
            final InputStream is = connection.getInputStream();
            rd = new BufferedReader(new InputStreamReader(is));
            final StringBuilder response = new StringBuilder(40); // or StringBuffer if not Java 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            return response.toString();
        } catch (final Throwable t) {
            LOG.warn("SprinklerHttpComm.executeGet() exception: ", t);
            return null;
        } finally {
            try {
                if (connection != null) connection.disconnect();
                if (wr != null) wr.close();
                if (rd != null) rd.close();
            } catch (Throwable t) {
                LOG.warn("SprinklerHttpComm.executeGet() finally{} exception: ", t);
            }
        }
    }

}
