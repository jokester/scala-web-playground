package io.jokester.scala_server_playground

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ HttpApp, Route }
import io.jokester.scala_server_playground.chatroom.ChatroomHandler
import io.jokester.scala_server_playground.blob.{ BlobHandler, BlobRepo, BlobRepoPG }
import io.jokester.scala_server_playground.hello.HelloHandler
import io.jokester.scala_server_playground.toy.{ ToyHandler, ToyRoute }
import io.jokester.scala_server_playground.util.RealWorld
import io.jokester.scala_server_playground.ws.WsEchoHandler
import scalikejdbc.config.DBs
import scalikejdbc.{ ConnectionPool, DB }

import scala.concurrent.ExecutionContextExecutor

object PlaygroundScalaServer extends HttpApp {

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
    helloHandler.route ~
      path("blob") {
        blobHandler.route
      } ~
      path("chatroom") {
        chat2Handler.route
      } ~
      path("ws-echo") {
        wsEchoHandler.route
      }
  }
}

object Bootstrap extends App {

  DBs.setupAll

  val interface = "localhost"
  val port = 18080

  PlaygroundScalaServer.startServer(interface, port)
  println(s"Server is now online at http://127.0.0.1:$port\nPress RETURN to stop...")

  println("Server is down...")

  PlaygroundScalaServer.system.terminate()
}
