import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class Esp8266HttpdTest {

    public static void main(String [ ] args) {
        Esp8266HttpdTest test = new Esp8266HttpdTest();
        final String fileName = "./esp8266_data.csv";
        PrintWriter stream = null;
        try {
            stream = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while(true) {
            JSONObject json = test.getJsonResponse();

            int state;
            if (json.containsKey("state")) {
                state = Integer.parseInt(json.getString("state"));

                int zone = 0;
                for (int pos = 0; pos<8; pos++) {
                    if (state == Math.pow(2, pos)) {
                        zone = pos + 1;
                    }
                }
                test.logData(stream, zone);
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private JSONObject getJsonResponse() {

        String targetUrl = "http://sprinklers.zhome.net/getall";
        String urlParameters = "";

        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoOutput(true);

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
            JSONObject json;
            json = JSONObject.fromObject(response.toString());

            return json;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new JSONObject();
    }

    private void logData(PrintWriter stream, int zone) {
        stream.println(new Date().toString() + "," + zone);
        stream.flush();
    }
}
