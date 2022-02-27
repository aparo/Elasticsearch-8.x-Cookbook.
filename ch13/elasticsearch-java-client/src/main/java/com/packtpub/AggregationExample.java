package com.packtpub;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class AggregationExample {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String index = "mytest";
        QueryHelper qh = new QueryHelper();
        qh.populateData(index);
        ElasticsearchClient client = qh.getClient();

        SearchRequest searchRequest = new SearchRequest.Builder().index(index).size(0)
                .aggregations("tag", t -> t.terms(terms -> terms.field("tag")))
                .aggregations("number1", t -> t.extendedStats(agg -> agg.field("number1"))).build();
        SearchResponse<QueryHelper.AppData> response = client.search(searchRequest, QueryHelper.AppData.class);


        System.out.println("Matched number of documents: " + response.hits().total().value());
        StringTermsAggregate termsAggs = response.aggregations().get("tag").sterms();
        System.out.println("Aggregation name: " + termsAggs._aggregateKind().name());
        System.out.println("Aggregation total: " + termsAggs.buckets().array().size());
        for (StringTermsBucket entry : termsAggs.buckets().array()) {
            System.out.println(" - " + entry.key() + " " + entry.docCount());
        }

        var extStats = response.aggregations().get("number1").extendedStats();
        System.out.println("Aggregation name: " + extStats._aggregateKind().name());
        System.out.println("Count: " + extStats.count());
        System.out.println("Min: " + extStats.min());
        System.out.println("Max: " + extStats.max());
        System.out.println("Standard Deviation: " + extStats.stdDeviation());
        System.out.println("Sum of Squares: " + extStats.sumOfSquares());
        System.out.println("Variance: " + extStats.variance());
        qh.dropIndex(index);


    }
}
