organization := "com.packtpub"

name := """alpakka-example"""

version := "0.0.5"

scalaVersion := "2.13.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

val scalaTestVersion = "3.2.8"
val alpakkaVersion = "3.0.4"
val akkaVersion = "2.6.14"

libraryDependencies ++= {
  Seq(
    "com.github.pathikrit" %% "better-files" % "3.9.1",
    "com.lightbend.akka" %% "akka-stream-alpakka-csv" % alpakkaVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % alpakkaVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % alpakkaVersion,
     "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  )
}

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo
)
