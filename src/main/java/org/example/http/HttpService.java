package org.example.http;

import okhttp3.*;

import java.io.IOException;

public class HttpService {

    private final OkHttpClient client;

    public HttpService(OkHttpClient client) {
        this.client = client;
    }

    public Call makeCall(String url, String method, RequestBody requestBody) {
        Request request = null;
        var requestBuilder = new Request.Builder().url(url);
        switch(method) {
            case "POST":
                request = requestBuilder.post(requestBody).build();
                break;
            case "PUT":
                request = requestBuilder.put(requestBody).build();
                break;
            case "PATCH":
                request = requestBuilder.patch(requestBody).build();
                break;
            case "GET":
                request = requestBuilder.get().build();
                break;
            default:
                throw new RuntimeException(String.format("Method %s not supported",method));
        }
        return this.client.newCall(request);
    }

    public String doRequest(String url, String method, RequestBody requestBody) {
        Response response = null;
        Call call = this.makeCall(url, method, requestBody);
        try {
            response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException("Could not complete the request", e);
        }
    }
}
