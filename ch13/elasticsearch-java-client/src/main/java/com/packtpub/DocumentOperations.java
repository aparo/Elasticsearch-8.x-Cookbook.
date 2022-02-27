package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.UpdateResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class DocumentOperations {
    public static class Record {
        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
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
                        .create(c -> c.index(index).mappings(m -> m.properties("text", t -> t.text(fn -> fn.store(true)))));
            } catch (IOException e) {
                System.out.println("Unable to create mapping");
            }

            final Record document = new Record();
            document.setText("unicorn");
            IndexResponse ir = client.index(c -> c.index(index).id("2").document(document));
            System.out.println("Version: " + ir.version());

            GetResponse<DocumentOperations.Record> gr = client.get(c -> c.index(index).id("2"), Record.class);
            System.out.println("Version: " + gr.version());

            UpdateResponse<DocumentOperations.Record> ur = client.update(u ->
                    u.index(index).id("2")
                            .scriptedUpsert(true)
                            .script(Script.of(s -> s.inline(code -> code.source("ctx._source.text = 'v2'")))),
                    Record.class
            );
            System.out.println("Version: " + ur.version());

            client.delete(d -> d.index(index).id("2"));
            io.deleteIndex(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
