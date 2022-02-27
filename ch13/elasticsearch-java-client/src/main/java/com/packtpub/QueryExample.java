package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class QueryExample {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String index = "mytest";
        QueryHelper qh = new QueryHelper();
        qh.populateData(index);
        ElasticsearchClient client = qh.getClient();

        SearchRequest searchRequest = new SearchRequest.Builder().index(index)
                .query(q ->
                        q.bool(b -> b
                                .must(must -> must.range(r -> r.field("number1").gte(JsonData.of(500)))).
                                filter(f -> f.term(t -> t.field("number2").value(FieldValue.of(1))))))
                .highlight(h -> h.fields("name", h1 -> h1.field("name"))).build();
        SearchResponse<QueryHelper.AppData> response = client.search(searchRequest, QueryHelper.AppData.class);

        System.out.println("Matched number of documents: " + response.hits().total().value());
        System.out.println("Maximum score: " + response.hits().maxScore());

        for (Hit<QueryHelper.AppData> hit : response.hits().hits()) {
            System.out.println("hit: " + hit.index() + ":" + hit.id());
        }
        qh.dropIndex(index);


    }
}
