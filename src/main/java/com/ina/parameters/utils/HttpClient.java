package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

@Slf4j
public class HttpClient {

    private static final Logger logger = LogManager.getLogger(HttpClient.class);
    String baseUrl = "http://38.9.60.146:9111/tmsengine/";

    public String exchange(String ep, String postData) {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, postData);
        Request request = new Request.Builder()
                .url(baseUrl + ep)
                .method("POST", body)
                .addHeader("Content-Type", "application/xml")
                .build();
        try {
            // send the request
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                return response.body().string();
            }
            else {
                log.info("resp code: %s", response.code());
                log.info("resp body: %s", response.body().toString());
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
