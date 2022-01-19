package org.example.client;

import okhttp3.OkHttpClient;

public class K8sClient {

    private final String server;
    private final OkHttpClient client;

    public K8sClient(String server, OkHttpClient client) {
        this.server = server;
        this.client = client;
    }

    public String getServer() {
        return server;
    }

    public OkHttpClient getClient() {
        return client;
    }

}
