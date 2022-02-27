package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


public class MappingOperations {

public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    String index = "mytest";
    ElasticsearchClient client = ClientHelper.createClient();
    IndicesOperations io = new IndicesOperations(client);
    try {
        if (io.checkIndexExists(index))
            io.deleteIndex(index);
        io.createIndex(index);
        try {
            PutMappingResponse response = client.indices()
                .putMapping(p -> p
                    .index(index)
                    .properties("nested1", m -> m.nested(f -> f))
                );
            if (!response.acknowledged()) {
                System.out.println("Something strange happens");
            }

        } catch (IOException e) {
            System.out.println("Unable to create mapping");
        }

        io.deleteIndex(index);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
}
