package io.jokester.scala_server_playground.ws

import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.actor._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import com.typesafe.scalalogging.LazyLogging
import io.jokester.scala_server_playground.ws.UserActor.ConnectionClose

class WsEchoHandler(implicit system: ActorSystem) extends LazyLogging {

  val userIdSeq = new AtomicInteger(0)

  def createComplicatedMsgFlow(): Flow[Message, Message, NotUsed] = {
    val userName = s"user-${userIdSeq.incrementAndGet()}"
    val userActor = system.actorOf(UserActor.props(), userName)

    val incoming = Flow[Message]
      .wireTap {
        logger.debug("incoming message {}", _)
      }
      .collect { case tm: TextMessage if tm.isStrict => tm }
      .to(Sink.actorRef(userActor, ConnectionClose))

    val outgoing = Source
      .actorRef[TextMessage](10, OverflowStrategy.fail)
      .mapMaterializedValue { outgoingActor =>
        userActor ! UserActor.OutgoingActor(outgoingActor)
      }
      .wireTap {
        logger.debug("outgoing message {}", _)
      }

    Flow.fromSinkAndSource(incoming, outgoing)
  }

  def route: Route = {
    path("ws1") {
      val echo = Flow[Message]
      handleWebSocketMessages(echo)
    } ~ path("ws2") {
      handleWebSocketMessages(createComplicatedMsgFlow())
    }
  }
}
