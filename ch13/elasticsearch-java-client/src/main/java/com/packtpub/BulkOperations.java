package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BulkOperations {
    public static class AppData {
        private int position;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

    }

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String index = "mytest";
        ElasticsearchClient client = ClientHelper.createClient();
        IndicesOperations io = new IndicesOperations(client);
        try {
            if (io.checkIndexExists(index))
                io.deleteIndex(index);
            try {
                client.indices()
                        .create(c -> c.index(index).mappings(m -> m
                                        .properties("position", p -> p.integer(i -> i.store(true)))
                                )
                        );
            } catch (IOException e) {
                System.out.println("Unable to create mapping");
            }

            List<BulkOperation> bulkOperations = new ArrayList<>();
            for (int i = 1; i <= 1000; i++) {
                final AppData obj = new AppData();
                obj.setPosition(i);
                bulkOperations.add(BulkOperation.of(o -> o.index(idx -> idx.index(index).id(Integer.toString(obj.position)).document(obj))));
            }

            System.out.println("Number of actions for index: " + bulkOperations.size());
            client.bulk(c -> c.operations(bulkOperations));

            bulkOperations.clear();

            for (int i = 1; i <= 1000; i++) {
                final int id = i;
                bulkOperations.add(BulkOperation.of(o -> o.update(u -> u.index(index).id(Integer.toString(id)).action(a -> a.script(s -> s.inline(code -> code.source("ctx._source.position += 2")))))));
            }
            System.out.println("Number of actions for update: " + bulkOperations.size());
            client.bulk(c -> c.operations(bulkOperations));

            bulkOperations.clear();
            for (int i = 1; i <= 1000; i++) {
                final int id = i;
                bulkOperations.add(BulkOperation.of(o -> o.delete(u -> u.index(index).id(Integer.toString(id)))));
            }
            System.out.println("Number of actions for delete: " + bulkOperations.size());
            client.bulk(c -> c.operations(bulkOperations));

            io.deleteIndex(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
