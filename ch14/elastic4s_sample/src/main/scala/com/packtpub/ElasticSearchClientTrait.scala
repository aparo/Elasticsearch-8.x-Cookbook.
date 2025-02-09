package com.packtpub

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.requests.common.HealthStatus
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import javax.net.ssl.SSLContext
import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.conn.ssl.TrustAllStrategy
import org.apache.http.conn.ssl.NoopHostnameVerifier

trait ElasticSearchClientTrait {
  lazy val callback = new HttpClientConfigCallback {
    override def customizeHttpClient(
        httpClientBuilder: HttpAsyncClientBuilder
    ): HttpAsyncClientBuilder = {
      val creds = new BasicCredentialsProvider()
      creds.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(
          sys.env.getOrElse("ES_USER", ""),
          sys.env.getOrElse("ES_PASSWORD", "")
        )
      )

      val sslContext = new SSLContextBuilder()
        .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
        .build();

      httpClientBuilder
        .setSSLContext(sslContext)
        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
        .setDefaultCredentialsProvider(creds)
    }
  }

  lazy val client = ElasticClient(
    JavaClient(
      ElasticProperties(s"https://localhost:9200"),
      requestConfigCallback = NoOpRequestConfigCallback,
      httpClientConfigCallback = callback
    )
  )

  def ensureIndexMapping(indexName: String): Unit = {
    if (
      client
        .execute {
          indexExists(indexName)
        }
        .await
        .result
        .isExists
    ) {
      client.execute {
        deleteIndex(indexName)
      }.await
    }

    client.execute {
      createIndex(indexName) shards 1 replicas 0 mapping (
        properties(
          textField("name") termVector "with_positions_offsets" stored true,
          longField("size"),
          doubleField("price"),
          geopointField("location"),
          keywordField("tag").copy(store = Some(true))
        )
      )
    }.await

    client.execute {
      clusterHealth() waitForStatus HealthStatus.Yellow
    }

  }

  def populateSampleData(indexName: String, size: Int = 1000): Unit = {
    import scala.util.Random
    val tags = List("cool", "nice", "bad", "awesome", "good")
    client.execute {
      bulk(0.to(size).map { i =>
        indexInto(indexName)
          .id(i.toString)
          .fields(
            "name" -> s"name_${i}",
            "size" -> (i % 10) * 8,
            "price" -> (i % 10) * 1.2,
            "location" -> List(
              30.0 * Random.nextDouble(),
              30.0 * Random.nextDouble()
            ),
            "tag" -> Random.shuffle(tags).take(3)
          )
      }: _*)
    }.await

    Thread.sleep(2000)

  }
}
