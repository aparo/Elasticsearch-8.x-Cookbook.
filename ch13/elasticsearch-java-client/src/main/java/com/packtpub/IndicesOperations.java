package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class IndicesOperations {
    private final ElasticsearchClient client;

    public IndicesOperations(ElasticsearchClient client) {
        this.client = client;
    }

    public boolean checkIndexExists(String name) throws IOException {
        return client.indices().exists(c -> c.index(name)).value();
    }

    public void createIndex(String name) throws IOException {
        client.indices().create(c -> c.index(name));
    }

    public void deleteIndex(String name) throws IOException {
        client.indices().delete(c -> c.index(name));
    }

    public void closeIndex(String name) throws IOException {
        client.indices().close(c -> c.index(name));
    }

    public void openIndex(String name) throws IOException {
        client.indices().open(c -> c.index(name));
    }

//    public void putMapping(String index, String source) throws IOException {
//        client.indices().putMapping(c -> c.index(index).p.source(source));
//    }


    public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ClientHelper nativeClient = new ClientHelper();
        ElasticsearchClient client = nativeClient.getClient();
        IndicesOperations io = new IndicesOperations(client);
        String myIndex = "test";
        if (io.checkIndexExists(myIndex))
            io.deleteIndex(myIndex);
        io.createIndex(myIndex);
        Thread.sleep(1000);
        io.closeIndex(myIndex);
        io.openIndex(myIndex);
        io.deleteIndex(myIndex);

        //we need to close the client to free resources
        nativeClient.close();

    }
}
