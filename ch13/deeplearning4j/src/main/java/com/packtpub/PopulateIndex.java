package com.packtpub;

import au.com.bytecode.opencsv.CSVReader;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class PopulateIndex {
    private static final Logger logger = getLogger(PopulateIndex.class);

    public boolean checkIndexExists(ElasticsearchClient client, String name) throws IOException {
        return client.indices().exists(c -> c.index(name)).value();
    }

    public void createIndex(ElasticsearchClient client, String name) throws IOException {
        client.indices().create(c -> c.index(name));
    }

    public void deleteIndex(ElasticsearchClient client, String name) throws IOException {
        client.indices().delete(c -> c.index(name));
    }

    public void populateIndex() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        ClientHelper nativeClient = new ClientHelper();
        ElasticsearchClient client = nativeClient.getClient();

        String indexName = "iris";
        if (!checkIndexExists(client, indexName))
            createIndex(client, indexName);

        InputStreamReader bReader = new InputStreamReader(getClass().getResourceAsStream("/" + "iris.txt"));

        List<BulkOperation> bulkOperations = new ArrayList<>();

        try (CSVReader reader = new CSVReader(bReader, ',', '\"')) {

            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                return;
            }
            int i = 0;
            for (i = 0; i < rows.size(); i++) {
                String[] line = rows.get(i);
                final Iris obj = new Iris(Integer.valueOf(line[4]), Float.valueOf(line[0]), Float.valueOf(line[1]), Float.valueOf(line[2]), Float.valueOf(line[3]));
                bulkOperations.add(BulkOperation.of(o -> o.index(idx -> idx.index(indexName).document(obj))));

            }
            client.bulk(c -> c.operations(bulkOperations));

        } catch (IOException e) {
            logger.warn("Error parsing CSV file", e);

            throw new IllegalStateException(e);
        }

        // we need to close the client to free resources
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        PopulateIndex pop = new PopulateIndex();
        pop.populateIndex();
        System.out.println("Data saved");
    }
}
