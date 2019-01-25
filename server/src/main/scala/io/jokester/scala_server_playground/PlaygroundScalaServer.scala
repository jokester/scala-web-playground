package io.jokester.scala_server_playground

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import com.typesafe.scalalogging.LazyLogging
import io.jokester.scala_server_playground.blob.{BlobHandler, BlobRepoPG}
import io.jokester.scala_server_playground.chatroom.ChatroomHandler
import io.jokester.scala_server_playground.hello.HelloHandler
import io.jokester.scala_server_playground.toy.ToyHandler
import io.jokester.scala_server_playground.util.RealWorld
import io.jokester.scala_server_playground.ws.WsEchoHandler
import scalikejdbc.config.DBs
import scalikejdbc.{ConnectionPool, DB, GlobalSettings, LoggingSQLAndTimeSettings}

import scala.concurrent.ExecutionContextExecutor

object PlaygroundScalaServer extends HttpApp {

  {
    // set log for scalikejdbc
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = true,
      singleLineMode = true,
      warningEnabled = true,
      logLevel = 'DEBUG
    )
  }

  implicit def system: ActorSystem = systemReference.get

  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  private implicit val entropy: RealWorld.type = RealWorld

  private def getDB = DB(ConnectionPool.borrow())

  private def blobHandler = {
    val repo = new BlobRepoPG(getDB _)
    new BlobHandler(repo)
  }

  private lazy val wsEchoHandler = new WsEchoHandler()

  private lazy val chat2Handler = new ChatroomHandler()

  private lazy val toyHandler = new ToyHandler(getDB _)

  private lazy val helloHandler = new HelloHandler

  override def routes: Route = extractMaterializer { implicit materializer =>
    pathPrefix("hello")(helloHandler.route) ~ // curl -vv '127.0.0.1:18080/hello
      pathPrefix("toy")(toyHandler.route) ~
      pathPrefix("blob")(blobHandler.route) ~
      pathPrefix("chatroom")(chat2Handler.route) ~
      pathPrefix("ws-echo")(wsEchoHandler.route)
  }
}

object Bootstrap extends App with LazyLogging {

  DBs.setupAll

  val interface = "localhost"
  val port = 18080

  // blocking until Enter
  PlaygroundScalaServer.startServer(interface, port)

  println("Server is down... terminating")
  PlaygroundScalaServer.system.terminate()
}
