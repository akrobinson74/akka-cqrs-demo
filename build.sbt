lazy val coffee = project
  .copy(id = "iris-cqrs-demo")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "iris-cqrs-demo"

libraryDependencies ++= Vector(
  Library.akkaHttp,
  Library.akkaPersistence,
  Library.akkaPersistenceCassandra,
  Library.akkaCamel,
  Library.camelRabbitMQ,
  Library.akkaHttpCirce,
  Library.circeGeneric,
  Library.circeParser,
  Library.circeJava8,
  Library.slick,
  Library.hikariCP,
  Library.mariaDb,
  Library.swaggerAkka,
  Library.akkaLog4j,
  Library.log4jCore,
  Library.slf4jLog4jBridge,
  Library.scalaCheck           % "test",
  Library.scalaTest            % "test",
  Library.akkaTestkit          % "test",
  Library.akkaHttpTestkit      % "test"
)

initialCommands := """|import nl.codecentric.coffee._
                      |""".stripMargin
