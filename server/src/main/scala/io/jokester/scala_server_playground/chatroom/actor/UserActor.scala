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
  private var userIdentity: Option[User] = None
  private var channelsWaitingJoin: Map[String, Int] = Map.empty
  private var channelsJoined: Map[UUID, ActorRef] = Map.empty

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
      this.userIdentity = Some(userInfo)
      outgoing.get ! ServerMessage.Authed(seq, userInfo)
      daemon ! UserAuthed(userInfo)
      context become authed
  }

  private def authed: Receive = wrapContext {
    case UserMessage.JoinChannel(seq: Int, name: String) =>
      channelsWaitingJoin += name -> seq
      daemon ! JoinRequest(from = userIdentity.get, name, self)
    case ChannelBroadcast(channel, users, messages) if channelsWaitingJoin.contains(channel.name) =>
      outgoing.get ! ServerMessage.JoinedChannel(channelsWaitingJoin(channel.name), channel, users, messages)
      channelsJoined += channel.uuid -> sender
      channelsWaitingJoin -= channel.name
    case ChannelBroadcast(channel, users, messages) if channelsJoined.contains(channel.uuid) =>
      nextServerSeq -= 1
      outgoing.get ! ServerMessage.BroadCast(
        nextServerSeq,
        users,
        Seq(channel),
        messages)
    case UserMessage.LeaveChannel(seq, channelUuid) if channelsJoined.contains(channelUuid) =>
      channelsJoined(channelUuid) ! LeaveRequest(userIdentity.get, channelUuid)
      outgoing.get ! ServerMessage.LeftChannel(seq, "voluntarily")
      channelsJoined -= channelUuid

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
