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

}
