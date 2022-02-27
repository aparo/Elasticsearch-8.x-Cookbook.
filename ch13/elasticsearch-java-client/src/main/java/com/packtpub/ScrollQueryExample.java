package com.packtpub;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import com.packtpub.QueryHelper.AppData;

public class ScrollQueryExample {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String index = "mytest";
        QueryHelper qh = new QueryHelper();
        qh.populateData(index);
        ElasticsearchClient client = qh.getClient();

        // Search
        SearchResponse<AppData> search = client.search(b -> b
                        .index(index).query(
                                q -> q.bool(bool -> bool.must(f -> f.range(range -> range.field("number1").gte(JsonData.of(500)))))
                        )
                , AppData.class
        );

        SearchRequest searchRequest = new SearchRequest.Builder().index(index)
                .query(q ->
                        q.bool(b -> b
                                .must(must -> must.range(r -> r.field("number1").gte(JsonData.of(500)))).
                                filter(f -> f.term(t -> t.field("number2").value(FieldValue.of(1))))))
                .size(30).scroll(Time.of(t -> t.time("2m"))).build();

        SearchResponse<QueryHelper.AppData> response = client.search(searchRequest, QueryHelper.AppData.class);

        do {
            for (Hit<AppData> hit : response.hits().hits()) {
                System.out.println("hit: " + hit.index() + ":" + hit.id());
            }
            final SearchResponse<QueryHelper.AppData> old_response=response;
            response = client.scroll( s -> s.scrollId(old_response.scrollId()), QueryHelper.AppData.class);

        } while (response.hits().hits().size() != 0); // Zero hits mark the end of the scroll and the while loop.

        qh.dropIndex(index);

    }
}
