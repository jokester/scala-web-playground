package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor._
import io.jokester.scala_server_playground.chatroom.{ Internal, ServerMessage, UserMessage }
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object UserActor {
  // type-checked and by-value way to create Props
  // see https://blog.codecentric.de/en/2017/03/akka-best-practices-defining-actor-props/
  def props(uuid: UUID, daemon: ActorRef) = Props(new UserActor(uuid, daemon))
}

class UserActor(uuid: UUID, daemon: ActorRef) extends Actor with ActorLogging with ActorLifecycleLogging {

  import Internal._

  private var nextServerSeq = -1
  private var outgoing: Option[ActorRef] = None
  private var userInfo: Option[User] = None
  private var joiningRoom: Map[String, Int] = Map.empty
  private var joinedRoom: Map[UUID, ActorRef] = Map.empty

  override def receive: Receive = wrapContext {
    case UserConnected(o) if outgoing.isEmpty =>
      log.info("user connected")
      this.outgoing = Some(o)
      context become connected
  }

  private def connected: Receive = wrapContext {
    // silly way to keep as a PartialFunction
    case UserMessage.Auth(seq, name, otp) if otp == "otp" =>
      val userInfo = User(name, uuid)
      this.userInfo = Some(userInfo)
      outgoing.get ! ServerMessage.Authed(seq, userInfo)
      daemon ! UserAuthed(userInfo)
      context become authed
  }

  private def authed: Receive = wrapContext {
    case UserMessage.JoinChannel(seq: Int, name: String) =>
      joiningRoom += name -> seq
      daemon ! JoinRequest(from = userInfo.get, name, self)
    case ChannelBroadcast(channel, users, messages) if joiningRoom.contains(channel.name) =>
      outgoing.get ! ServerMessage.JoinedChannel(joiningRoom(channel.name), channel, users, messages)
      joinedRoom += channel.uuid -> sender
      joiningRoom -= channel.name
    case ChannelBroadcast(channel, users, messages) if joinedRoom.contains(channel.uuid) =>
      nextServerSeq -= 1
      outgoing.get ! ServerMessage.BroadCast(
        nextServerSeq,
        users,
        Seq(channel),
        messages)
    case UserMessage.LeaveChannel(seq, channelUuid) if joinedRoom.contains(channelUuid) =>
      joinedRoom(channelUuid) ! LeaveRequest(userInfo.get, channelUuid)
      outgoing.get ! ServerMessage.LeftChannel(seq, "voluntarily")
      joinedRoom -= channelUuid

  }

  private def hookBeforeReceive: Receive = {
    case m: UserDisconnected if m.userUuid == uuid =>
      log.debug("user disconnected, stopping")
      daemon ! m
      context.stop(self)
    case UserMessage.Ping(seqNo) =>
      outgoing.get ! ServerMessage.Pong(seqNo)
  }

  private def hookAfterReceive: Receive = {
    case m: UserMessage.FromUser =>
      log.warning("unhandled message: {}", m.seq)
      outgoing.get ! ServerMessage.Fail(m.seq, Seq("unhandled message"))
  }

  private def wrapContext(inner: Receive): Receive = {
    hookBeforeReceive orElse inner orElse hookAfterReceive
  }
}
