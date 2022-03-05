package com.packtpub;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


public class QueryCreation {

    public static class Record {
        private String text;
        private int number1;
        private int number2;

        public Record(String text, int number1, int number2) {
            this.text = text;
            this.number1 = number1;
            this.number2 = number2;
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
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String index = "mytest";
        ElasticsearchClient client = ClientHelper.createClient();
        IndicesOperations io = new IndicesOperations(client);
        try {
            if (io.checkIndexExists(index))
                io.deleteIndex(index);
            try {
                client.indices()
                        .create(c -> c.index(index).mappings(m -> m
                                .properties("text",  t -> t.text(tx -> tx.store(true)))
                                .properties("number1",  t -> t.integer(tx -> tx.store(true)))
                                .properties("number2",  t -> t.integer(tx -> tx.store(true)))
                        ));
            } catch (IOException e) {
                System.out.println("Unable to create mapping");
            }

            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                final Record obj = new Record(Integer.toString(i), i+1, i%2);
                bulkOperations.add(BulkOperation.of(o -> o.index(idx -> idx.index(index).id(obj.text).document(obj))));
            }
            client.bulk(c -> c.operations(bulkOperations));

            client.indices().refresh(r -> r.index(index));
SearchRequest searchRequest = new SearchRequest.Builder()
        .index(index)
        .query(q ->
                q.bool(b -> b
                        .must(must ->
                                must.range(r -> r.field("number1").gte(JsonData.of(500)))
                        ).
                        filter(f -> f.term(t -> t.field("number2").value(FieldValue.of(1))))
                )
        )
        .build();


SearchResponse<Record> response = client.search(searchRequest, Record.class);
assert response.hits().total() != null;
System.out.println("Matched records of elements: " + response.hits().total().value());

            SearchResponse<Record> response2 = client.search(new SearchRequest.Builder().index(index).build(), Record.class);
            assert response2.hits().total() != null;
            System.out.println("Matched records of elements: " + response2.hits().total().value());

            io.deleteIndex(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
