package io.jokester.scala_server_playground.chatroom

import akka.NotUsed
import akka.actor.{ActorSystem, PoisonPill}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import com.typesafe.scalalogging.LazyLogging
import io.jokester.scala_server_playground.chatroom.actor.{DaemonActor, UserActor}
import io.jokester.scala_server_playground.util.Entropy

/**
  * A chat app that
  * - have multiple chatrooms
  * - have (semi-) persisted storage (pgsql / redis)
  * - uses guardrail generated code to decode/encode websocket message
  */
class ChatroomHandler(implicit system: ActorSystem, implicit val entropy: Entropy)
  extends WsInterface with LazyLogging {

  import Internal._

  private lazy val daemon = system.actorOf(DaemonActor.props(entropy))

  def newUserFlow()(implicit m: Materializer): Flow[Message, Message, NotUsed] = {

    // FIXME: userActor is not stopped until server down
    val userUuid = entropy.nextUUID()
    val userActor = system.actorOf(UserActor.props(userUuid, daemon))

    val incoming = Flow[Message]
      .mapAsync(1)(retrieveCompleteMessage)
      .map { msg => decode(msg.text) }
      .wireTap { decoded =>
        decoded.fold(
          err => logger.warn("error decoding message", err),
          msg => logger.debug("got message: {}", msg))
      }
      //      .filter(_.isRight)
      //      .map(_.right.get)
      .flatMapConcat { decoded =>
      decoded.fold(
        _ => Source.empty,
        msg => Source.single(msg),
      )
    }
      .to(Sink.actorRef(userActor, PoisonPill))

    val outgoing =
      Source.actorRef[ServerMessage.ToUser](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          userActor ! UserConnected(outActor)
        }
        .wireTap(msg => logger.debug("sending message: {}", msg))
        .map(encode)

    Flow.fromSinkAndSource(incoming, outgoing)
  }

  def route: Route = path("ws") {
    extractMaterializer { implicit m =>
      get {
        handleWebSocketMessages(newUserFlow)
      }
    }
  }
}