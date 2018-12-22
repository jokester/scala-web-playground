package io.jokester.learning.scala_server

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ HttpApp, Route }
import akka.stream.ActorMaterializer
import io.jokester.learning.scala_server.chat2.Chat2Handler
import io.jokester.learning.scala_server.repo.{ BlobRepoImpl, ToySynchronizedMemoryRepo }
import io.jokester.learning.scala_server.route._
import io.jokester.learning.scala_server.util.RealWorld
import scalikejdbc.config.DBs
import scalikejdbc.{ ConnectionPool, DB }

import scala.concurrent.ExecutionContextExecutor

object PlaygroundScalaServer extends HttpApp {

  implicit def system: ActorSystem = systemReference.get

  implicit def executionContext: ExecutionContextExecutor = system.dispatcher

  private implicit val entropy: RealWorld.type = RealWorld

  private def getDB = DB(ConnectionPool.borrow())

  private def blobHandler = {
    val repo = new BlobRepoImpl(getDB _)
    new BlobHandler(repo)
  }

  private def wsHandler = new WebSocketHandler(system)

  private lazy val chat2Handler = new Chat2Handler

  override def routes: Route = extractMaterializer { implicit materializer =>
    blobHandler.route ~
      wsHandler.route ~
      //      chatHandler.route ~
      chat2Handler.route ~
      FailRoute.route
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
