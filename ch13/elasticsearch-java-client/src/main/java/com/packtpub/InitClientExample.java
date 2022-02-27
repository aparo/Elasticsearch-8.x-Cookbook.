package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;

public class InitClientExample {
    public static void main(String[] args) throws IOException {
        HttpHost httpHost = new HttpHost("localhost", 9200, "http");
        RestClientBuilder restClient = RestClient.builder(httpHost);

        ElasticsearchTransport transport = new RestClientTransport(
                restClient.build(),
                new JacksonJsonpMapper()
        );

        ElasticsearchClient client = new ElasticsearchClient(transport);

    }
}
