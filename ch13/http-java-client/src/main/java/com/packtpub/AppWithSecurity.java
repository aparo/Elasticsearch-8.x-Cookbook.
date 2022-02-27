package com.packtpub;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class AppWithSecurity {

    private static String wsUrl = "https://127.0.0.1:9200";

    public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        CloseableHttpClient client = HttpClients.custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).setRetryHandler(new MyRequestRetryHandler())
                .build();
        HttpGet method = new HttpGet(wsUrl + "/mybooks/_doc/1");
        // Execute the method.

        HttpHost targetHost = new HttpHost("localhost", 9200, "https");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();

        credsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(System.getenv("ES_USER"), System.getenv("ES_PASSWORD")));
        // Create AuthCache instance

        AuthCache authCache = new BasicAuthCache();

        // Generate BASIC scheme object and add it to local auth cache

        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);

        method.addHeader("Accept-Encoding", "gzip");

        try {
            CloseableHttpResponse response = client.execute(method, context);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + response.getStatusLine());
            } else {
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);
                System.out.println(responseBody);
            }

        } catch (IOException e) {
            System.err.println("Fatal transport error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
    }
}
