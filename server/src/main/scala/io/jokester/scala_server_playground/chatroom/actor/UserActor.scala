package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor._
import akka.pattern.ask
import io.jokester.scala_server_playground.chatroom.{ Internal, ServerMessage, UserMessage }
import io.jokester.scala_server_playground.util.{ ActorLifecycleLogging, Entropy, RealWorld }

object UserActor {
  // type-checked and by-value way to create Props
  // see https://blog.codecentric.de/en/2017/03/akka-best-practices-defining-actor-props/
  def props(uuid: UUID, daemon: ActorRef) = Props(new UserActor(uuid, daemon, e = RealWorld))
}

class UserActor(uuid: UUID, daemon: ActorRef, e: Entropy) extends Actor with ActorLogging with ActorLifecycleLogging {

  import Internal._

  private var _nextServerMessageSeq = -1
  private var outgoing: Option[ActorRef] = None
  private var userIdentity: Option[User] = None
  private var channelsJoining: Map[String, Int] = Map.empty
  private var channelsJoined: Map[UUID, ActorRef] = Map.empty
  private var messagesSending: Map[UUID, (Int, ChatMessage)] = Map.empty

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
      channelsJoining += name -> seq
      daemon ! JoinRequest(from = userIdentity.get, name, self)

    case UserMessage.LeaveChannel(seq, channelUuid) if channelsJoined.contains(channelUuid) =>
      daemon ! LeaveRequest(userIdentity.get, channelUuid)
      outgoing.get ! ServerMessage.LeftChannel(seq, "voluntarily")
      channelsJoined -= channelUuid

    case UserMessage.SendChatMessage(seq, channelUuid, text) if channelsJoined contains channelUuid =>
      val msg = ChatMessage(
        uuid = e.nextUUID(),
        userUuid = userIdentity.get.uuid,
        channelUuid = channelUuid,
        text = text,
        timestamp = e.currentServerTime())
      messagesSending += msg.uuid -> (seq, msg)
      channelsJoined(channelUuid) ! SendMessageRequest(userIdentity.get, msg)

    case SendMessageResponse(messageUuid) if messagesSending contains messageUuid =>
      val (seq, msg) = messagesSending(messageUuid)
      messagesSending -= messageUuid
      outgoing.get ! ServerMessage.SentMessage(seq, msg)

    case b @ ChannelBroadcast(channel, users, messages) if channelsJoining.contains(channel.name) =>
      outgoing.get ! ServerMessage.JoinedChannel(channelsJoining(channel.name), channel, users, messages)
      channelsJoined += channel.uuid -> sender
      channelsJoining -= channel.name
      forwardChannelBroadcast(b)

    case b @ ChannelBroadcast(channel, _, _) if channelsJoined.contains(channel.uuid) =>
      forwardChannelBroadcast(b)

  }

  private def nextServerMessageSeq() = {
    _nextServerMessageSeq -= 1
    _nextServerMessageSeq
  }

  private def forwardChannelBroadcast(b: ChannelBroadcast): Unit = {
    outgoing.get ! ServerMessage.BroadCast(
      nextServerMessageSeq(),
      b.users,
      Seq(b.from),
      b.messages)
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
