
package src.rhino.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zhanjun on 2017/7/6.
 */
public class HttpClient {

    private String httpUrl;
    private static int timeout = 30000;

    public HttpClient(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public void doPost(String params) throws Exception {
        doPost(params, false);
    }

    public void doPost(String params, boolean isJson) throws Exception {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        if (isJson) {
            connection.setRequestProperty("Content-type", "application/json");
        } else {
            connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        }
        connection.setRequestProperty("Charset", "UTF-8");
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.connect();
        try (OutputStream outStm = connection.getOutputStream()) {
            outStm.write(params.getBytes("UTF-8"));
            outStm.flush();
        }
        try (InputStream inStm = connection.getInputStream()) {
            //Do nothing
        }
        connection.disconnect();
    }
}
