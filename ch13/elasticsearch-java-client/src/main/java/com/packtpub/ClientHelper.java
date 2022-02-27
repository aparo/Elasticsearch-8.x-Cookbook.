package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class ClientHelper {
    public static ElasticsearchClient createClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestClientBuilder clientBuilder = RestClient.builder(new HttpHost("localhost", 9200, "https"))
                .setCompressionEnabled(true);
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(System.getenv("ES_USER"), System.getenv("ES_PASSWORD")));
        final SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build();
        clientBuilder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {

            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                return httpClientBuilder
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .setDefaultCredentialsProvider(credentialsProvider);
            }
        });
        RestClient client = clientBuilder.build();

        // Create the new Java Client with the same low level client
        ElasticsearchTransport transport = new RestClientTransport(
                client,
                new JacksonJsonpMapper()
        );


        return new ElasticsearchClient(transport);

    }

    private final ElasticsearchClient client;

    public ClientHelper() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        client = ClientHelper.createClient();
    }

    public ElasticsearchClient getClient() {
        return client;
    }

    public void close() throws IOException {
    }
}
