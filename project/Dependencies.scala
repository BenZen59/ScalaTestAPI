import sbt.*
import sbt.Keys.libraryDependencies

object Dependencies {
  lazy val munit = "org.scalameta" %% "munit" % "0.7.29"
  lazy val akkaHttpVersion = "10.5.2"
  lazy val akkaVersion = "2.8.2"
      libraryDependencies ++= Seq(
        "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
        "com.typesafe.akka" %% "akka-stream" % akkaVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",

        "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
        "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
        "org.scalatest" %% "scalatest" % "3.1.4" % Test

    )


}
