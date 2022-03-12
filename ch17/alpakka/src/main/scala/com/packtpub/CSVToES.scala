package com.packtpub

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.alpakka.elasticsearch._
import akka.stream.alpakka.elasticsearch.scaladsl._
import akka.stream.scaladsl.Source
import akka.util.ByteString
import better.files.Resource

import scala.concurrent.Await
import scala.concurrent.duration._

final case class Iris(
    label: String,
    f1: Double,
    f2: Double,
    f3: Double,
    f4: Double
)

object CSVToES extends App {

  implicit val actorSystem = ActorSystem()
  implicit val actorMaterializer = ActorMaterializer()
  implicit val executor = actorSystem.dispatcher

val connectionSettings =
  ElasticsearchConnectionSettings("http://localhost:9200")
    .withCredentials(
      sys.env.getOrElse("ES_USER", "elastic"),
      sys.env.getOrElse("ES_PASSWORD", "password")
    )

  final case class Iris(
      label: String,
      f1: Double,
      f2: Double,
      f3: Double,
      f4: Double
  )

  import spray.json._
  import DefaultJsonProtocol._

  implicit val format: JsonFormat[Iris] = jsonFormat5(Iris)

  val sinkSettings =
    ElasticsearchWriteSettings(connectionSettings)
      .withBufferSize(1000)
      .withVersionType("internal")
      .withRetryLogic(
        RetryAtFixedRate(maxRetries = 5, retryInterval = 1.second)
      )

  val graph = Source
    .single(ByteString(Resource.getAsString("com/packtpub/iris.csv")))
    .via(CsvParsing.lineScanner())
    .drop(1)
    .map(values =>
      WriteMessage.createIndexMessage[Iris](
        Iris(
          values(4).utf8String,
          values.head.utf8String.toDouble,
          values(1).utf8String.toDouble,
          values(2).utf8String.toDouble,
          values(3).utf8String.toDouble
        )
      )
    )
    .runWith(
      ElasticsearchSink.create[Iris](
        ElasticsearchParams.V7("iris-alpakka"),
        settings = sinkSettings
      )
    )

  val finish = Await.result(graph, Duration.Inf)
  actorSystem.terminate()
  Await.result(actorSystem.whenTerminated, Duration.Inf)

}
