package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch._types.mapping.TermVectorOption;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import org.elasticsearch.client.RequestOptions;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QueryHelper {
    private final ElasticsearchClient client;
    private final IndicesOperations io;

    public QueryHelper() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        this.client = ClientHelper.createClient();
        io = new IndicesOperations(client);
    }

    private final String[] tags = new String[]{"nice", "cool", "bad", "amazing"};

    private String getTag() {
        return tags[new Random().nextInt(tags.length)];
    }

    public void populateData(String index) throws IOException {
        if (io.checkIndexExists(index))
            io.deleteIndex(index);

        try {
            client.indices()
                    .create(
                            c -> c.index(index).mappings(m -> m
                                    .properties("name", map -> map.text(t -> t.termVector(TermVectorOption.WithPositionsOffsets).store(true)))
                                    .properties("tag", map -> map.keyword(t -> t.ignoreAbove(250)))
                            )
                    );
        } catch (IOException e) {
            System.out.println("Unable to create mapping");
        }
        client.cluster().health(c -> c.index(index).waitForStatus(HealthStatus.Green));

        List<BulkOperation> bulkOperations = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            final AppData obj = new AppData(Integer.toString(i),Integer.toString(i), i+1, i%2,  getTag());
            bulkOperations.add(BulkOperation.of(o -> o.index(idx -> idx.index(index).id(obj.id).document(obj))));
        }

        System.out.println("Number of actions for index: " + bulkOperations.size());
        client.bulk(c -> c.operations(bulkOperations));

        client.indices().refresh(c -> c.index(index));
    }

    public void dropIndex(String index) throws IOException {
        io.deleteIndex(index);
    }

    public ElasticsearchClient getClient() {
        return client;
    }

    public static class AppData {
        private String id;
        private String text;
        private int number1;
        private int number2;
        private String tag;

        public AppData(String id, String text, int number1, int number2, String tag) {
            this.id = id;
            this.text = text;
            this.number1 = number1;
            this.number2 = number2;
            this.tag = tag;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getNumber1() {
            return number1;
        }

        public void setNumber1(int number1) {
            this.number1 = number1;
        }

        public int getNumber2() {
            return number2;
        }

        public void setNumber2(int number2) {
            this.number2 = number2;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
