import java.io.File
import com.typesafe.config.ConfigFactory

name := "scala-server"
version := "0.0.1"

scalaVersion := "2.12.7"
scalacOptions ++= Seq("-Xlint")

lazy val akkaHttpVersion = "10.1.5"
lazy val akkaVersion    = "2.5.19"
lazy val scalikejdbcVersion = "3.3.1"
lazy val catsVersion = "1.4.0"
lazy val circeVersion = "0.10.1"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "io.jokester", scalaVersion    := "2.12.6"
    )),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"           % akkaVersion,

      // scalikejdbc and deps
      "org.scalikejdbc"   %% "scalikejdbc"          % scalikejdbcVersion,
      "org.scalikejdbc"   %% "scalikejdbc-config"   % scalikejdbcVersion,
      "org.scalikejdbc"   %% "scalikejdbc-test"     % scalikejdbcVersion,
      "org.postgresql"    %  "postgresql"           % "42.2.2",

      // guardrail
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.typelevel" %% "cats-core" % catsVersion,

      // util
      "com.gilt" %% "gfc-guava" % "0.3.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
      "ch.qos.logback" % "logback-classic" % "1.2.3", // this provides SLJ4J backend

      // test
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )

/** flyway */
val configFile = new File(sys.env.getOrElse("CONFIG_FILE", "src/main/resources/application.conf"))
val defaultDb = ConfigFactory.parseFile(configFile).getConfig("db.default").resolve()

enablePlugins(FlywayPlugin)
flywayLocations += "db/migration"
flywayUrl := defaultDb.getString("url")
flywayUser := defaultDb.getString("user")
flywayPassword := defaultDb.getString("password")

val testDb = ConfigFactory.parseFile(configFile).getConfig("db.test").resolve()
flywayUrl in Test := testDb.getString("url")
flywayUser in Test := testDb.getString("user")
flywayPassword in Test := testDb.getString("password")

guardrailTasks in Compile := List(
  // Server(file("../api/src/v1-api.swagger.json"), pkg="io.jokester.learning.scala_server.api_v1", tracing=false),
  Server(file("../api/src/chat2.swagger.json"), pkg="io.jokester.learning.scala_server.chat2", tracing=false)
)

enablePlugins(ScalikejdbcPlugin)

// D: print duration  / F: full stack trace
testOptions in Test += Tests.Argument("-oDF")

