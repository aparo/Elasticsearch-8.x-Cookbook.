package com.packtpub

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.elasticsearch.scaladsl.ElasticsearchSource
import akka.stream.alpakka.elasticsearch.{ElasticsearchConnectionSettings, ElasticsearchParams, ElasticsearchSourceSettings}
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import com.mongodb.reactivestreams.client.MongoClients
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._

import scala.concurrent.Await
import scala.concurrent.duration._

object ESToMongoDB extends App {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = actorSystem.dispatcher

val connectionSettings =
  ElasticsearchConnectionSettings("http://localhost:9200")
    .withCredentials(
      sys.env.getOrElse("ES_USER", "elastic"),
      sys.env.getOrElse("ES_PASSWORD", "password")
    )

  import spray.json._
  import DefaultJsonProtocol._

  implicit val format: JsonFormat[Iris] = jsonFormat5(Iris)
  val codecRegistry =
    fromRegistries(fromProviders(classOf[Iris]), DEFAULT_CODEC_REGISTRY)

  private val mongo = MongoClients.create("mongodb://localhost:27017")
  private val db = mongo.getDatabase("es-to-mongo")
  val irisCollection = db
    .getCollection("iris", classOf[Iris])
    .withCodecRegistry(codecRegistry)

  val sourceSettings =
    ElasticsearchSourceSettings(connectionSettings)
      .withBufferSize(1000)

val graph =
  ElasticsearchSource
    .typed[Iris](
      ElasticsearchParams.V7("iris-alpakka"),
      query = """{"match_all": {}}""",
      settings = sourceSettings
    )
    .map(_.source) // we want only the source
    .grouped(100) // bulk insert of 100
    .runWith(MongoSink.insertMany[Iris](irisCollection))

  val finish = Await.result(graph, Duration.Inf)

  mongo.close()
  actorSystem.terminate()
  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
