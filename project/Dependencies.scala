import sbt._

object Version {
  final val Akka = "2.5.17"
  final val AkkaHttp = "10.1.5"
  final val AkkaHttpJson = "1.22.0"
  final val AkkaLog4j = "1.6.1"
  final val AkkaPersistenceCassandra = "0.90"
  final val Alpakka = "1.0-M1"
  final val Circe = "0.10.0"
  final val CamelRabbitMQ = "2.22.1"
  final val HikariCP = "3.2.0"
  final val Log4j = "2.6"
  final val Log4jCore = "2.11.1"
  final val MariaDB = "2.3.0"
  final val OPRabbit = "2.1.0"
  final val RabbitMQ = "5.0.0"
  final val Scala = "2.12.6"
  final val ScalaCheck = "1.14.0"
  final val ScalaTest = "3.0.5"
  final val Slick = "3.2.3"
  final val Streamz = "0.10-M1"
  final val SwaggerAkka = "1.0.0"
}

object Library {
  val akkaCamel = "com.typesafe.akka" %% "akka-camel" % Version.Akka
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.AkkaHttp
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.AkkaHttp
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.Akka
  val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.AkkaPersistenceCassandra
  val akkaPersistenceCassandraTest = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.AkkaPersistenceCassandra
  val akkaStreams = "com.typesafe.akka" %% "akka-stream" % Version.Akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.Akka
  val alpakkaAMQP = "com.lightbend.akka" %% "akka-stream-alpakka-amqp" % Version.Alpakka
  val camelRabbitMQ = "org.apache.camel" % "camel-rabbitmq" % Version.CamelRabbitMQ
  val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.AkkaHttpJson
  val circeGeneric = "io.circe" %% "circe-generic" % Version.Circe
  val circeParser = "io.circe" %% "circe-parser" % Version.Circe
  val circeJava8 = "io.circe" %% "circe-java8" % Version.Circe
  val slick = "com.typesafe.slick" %% "slick" % Version.Slick
  val hikariCP = "com.zaxxer" % "HikariCP" % Version.HikariCP
  val mariaDb = "org.mariadb.jdbc" % "mariadb-java-client" % Version.MariaDB
  val swaggerAkka = "com.github.swagger-akka-http" %% "swagger-akka-http" % Version.SwaggerAkka
  val opRabbit = "com.spingo" %% "op-rabbit-core" % Version.OPRabbit
  val akkaLog4j = "de.heikoseeberger" %% "akka-log4j" % Version.AkkaLog4j
  val log4jCore = "org.apache.logging.log4j" % "log4j-core" % Version.Log4jCore
  val rabbitMQ = "com.newmotion" %% "akka-rabbitmq" % Version.RabbitMQ
  val slf4jLog4jBridge = "org.apache.logging.log4j" % "log4j-slf4j-impl" % Version.Log4jCore
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.ScalaCheck
  val scalaTest = "org.scalatest" %% "scalatest" % Version.ScalaTest
}
