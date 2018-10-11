import CompilerFlags._

name := "akka-cqrs-demo"
organization := "com.olx"
version := "1.0"

scalaVersion := "2.12.6"
scalacOptions ++= compilerFlags

lazy val akkaVersion = "2.5.16"
lazy val akkaHttpVersion = "10.1.5"
lazy val circeVersion = "0.9.3"
lazy val akkaJsonVersion = "1.20.1"

libraryDependencies ++= Vector(
  Library.alpakkaAMQP,
  Library.akkaCamel,
  Library.akkaHttp,
  Library.akkaHttpCirce,
  Library.akkaLog4j,
  Library.akkaPersistence,
  Library.akkaPersistenceCassandra,
  Library.akkaStreams,
  Library.camelRabbitMQ,
  Library.circeGeneric,
  Library.circeJava8,
  Library.circeParser,
  Library.hikariCP,
  Library.log4jCore,
  Library.mariaDb,
  Library.opRabbit,
  Library.rabbitMQ,
  Library.slf4jLog4jBridge,
  Library.slick,
  Library.swaggerAkka,
  Library.akkaHttpTestkit % "test",
  Library.akkaPersistenceCassandraTest % "test",
  Library.akkaTestkit % "test",
  Library.scalaCheck % "test",
  Library.scalaTest % "test"
)
