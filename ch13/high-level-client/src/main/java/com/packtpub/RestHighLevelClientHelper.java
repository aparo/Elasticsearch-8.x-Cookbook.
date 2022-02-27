package com.packtpub;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;

public class RestHighLevelClientHelper {
    public static RestHighLevelClient createHighLevelClient() {

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "https"), new HttpHost("localhost", 9201, "https")));

    }

    private final RestHighLevelClient client;

    public RestHighLevelClientHelper() {

        client = RestHighLevelClientHelper.createHighLevelClient();
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    public void close() throws IOException {
        client.close();
    }
}
