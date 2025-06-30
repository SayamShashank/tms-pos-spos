package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HttpClient {

    private final OkHttpClient client;

    String baseUrl = "http://38.9.60.146:9111/tmsengine/";


    @Autowired
    public HttpClient(OkHttpClient mockOkHttpClient) {
        this.client = mockOkHttpClient;
    }

    public String exchange(String ep, String postData) {
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(postData, mediaType);
        Request request = new Request.Builder()
                .url(baseUrl + ep)
                .method("POST", body)
                .addHeader("Content-Type", "application/xml")
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.code() == 200) {
                return response.body().string();
            } else {
                log.info("resp code: %s", response.code());
                log.info("resp body: %s", response.body().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
