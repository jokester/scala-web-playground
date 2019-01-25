package io.jokester.scala_server_playground.chatroom

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives.{get, handleWebSocketMessages, path, _}
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import com.redis.{RedisClient, RedisClientPool}
import com.typesafe.scalalogging.LazyLogging
import io.jokester.scala_server_playground.chatroom.actor.{DaemonActor, UserActor}
import io.jokester.scala_server_playground.conf.ServerConf
import io.jokester.scala_server_playground.util.Entropy

import scala.util.{Failure, Success}

/**
  * A chat app that
  * - have multiple chatrooms
  * - have (semi-) persisted storage (pgsql / redis)
  * - uses guardrail generated code to decode/encode websocket message
  */
class ChatroomHandler(implicit
                      system: ActorSystem,
                      implicit val entropy: Entropy)
    extends WsInterface
    with LazyLogging {

  import Internal._

  val redisClientPool: RedisClientPool = {
    val conf = ServerConf.redisConf.get
    new RedisClientPool(host = conf.host, port = conf.port)
  }

  val redisClient: RedisClient = {
    val conf = ServerConf.redisConf.get
    new RedisClient(host = conf.host, port = conf.port)
  }

  logger.info("redis initialized: {} / {}", redisClient, redisClientPool)

  val repo = new ChatroomPersistRepo(ServerConf.getDB)

  private lazy val daemon = system.actorOf(DaemonActor.props(repo))

  private lazy val userCounter = new AtomicInteger(1)

  def newUserFlow()(
    implicit
    m: Materializer
  ): Flow[Message, Message, NotUsed] = {

    repo.cleanObject(entropy.currentServerTime().minusSeconds(3600L * 24 * 7))

    // FIXME: userActor is not stopped until server down
    val userActor =
      system.actorOf(UserActor.props(daemon, repo), s"UserActor-${userCounter.incrementAndGet()}")

    val incoming = Flow[Message]
      .mapAsync(1)(retrieveCompleteMessage)
      .map { msg =>
        decode(msg.text)
      }
      .wireTap { decoded =>
        decoded.fold(
          err => logger.warn("error decoding message", err),
          msg =>
            if (!msg.isInstanceOf[UserMessage.Ping])
              logger.debug("got message: {}", msg)
        )
      }
      //      .filter(_.isRight)
      //      .map(_.right.get)
      .flatMapConcat { decoded =>
        decoded.fold(_ => Source.empty, msg => Source.single(msg))
      }
      .to(Sink.actorRef(userActor, UserDisconnected(None)))

    val outgoing =
      Source
        .actorRef[ServerMessage.ToUser](10, OverflowStrategy.fail)
        .mapMaterializedValue { outActor =>
          userActor ! UserConnected(outActor)
        }
        .wireTap(
          msg =>
            if (!msg.isInstanceOf[ServerMessage.Pong])
              logger.debug("sending message: {}", msg)
        )
        .map(encode)

    Flow.fromSinkAndSource(incoming, outgoing)
  }

  def wsRoute: Route = path("ws") {
    extractMaterializer { implicit m =>
      get {
        handleWebSocketMessages(newUserFlow)
      }
    }
  }

  def httpRoute: Route = pathPrefix("admin") {
    (get & path("online-count")) {
      implicit val timeout: Timeout = askTimeout
      onComplete(daemon ? AdminQueryUserCount()) {
        case Success(value) =>
          complete(value.toString)
        case Failure(exception) =>
          failWith(exception)
      }
    }
  }

  def askTimeout = Timeout(2, TimeUnit.SECONDS)

  def route: Route = wsRoute ~ httpRoute
}
