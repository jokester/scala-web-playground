package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor._
import io.jokester.scala_server_playground.chatroom.{ Internal, ServerMessage, UserMessage }
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object UserActor {
  // type-checked and by-value way to create Props
  // see https://blog.codecentric.de/en/2017/03/akka-best-practices-defining-actor-props/
  def props(uuid: UUID) = Props(new UserActor(uuid))
}

class UserActor(uuid: UUID) extends Actor with ActorLogging with ActorLifecycleLogging {

  import Internal._
  import ServerMessage._
  import UserMessage._

  var joinedRooms = Map.empty[String, ActorRef]
  var outgoing: Option[ActorRef] = None
  var userInfo: Option[User] = None

  override def receive: Receive = wrapContext {
    case UserConnected(o) if outgoing.isEmpty =>
      log.info("user connected")
      this.outgoing = Some(o)
      context become connected
  }

  private def connected: Receive = wrapContext {
    // silly way to keep as a PartialFunction
    case Auth(seq, name, otp) if otp == "otp" =>
      val userInfo = User(name, uuid)
      this.userInfo = Some(userInfo)
      outgoing.get ! Authed(seq, userInfo)
      context become authed
  }

  private def authed: Receive = wrapContext {
    case msg if msg == "EEE" =>
  }

  private def handlePing: Receive = {
    case Ping(seqNo) =>
      outgoing.get ! Pong(seqNo)
  }

  private def catchUnhandledSeq: Receive = {
    case m: FromUser =>
      log.warning("unhandled message: {}", m.seq)
      outgoing.get ! Fail(m.seq, Seq("unhandled message"))
  }

  private def wrapContext(inner: Receive): Receive = {
    handlePing orElse inner orElse catchUnhandledSeq
  }

  private def in(room: String) = joinedRooms.contains(room)
}
