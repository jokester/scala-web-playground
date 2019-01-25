package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor._
import io.jokester.scala_server_playground.chatroom.{ChatroomRepo, Internal, ServerMessage, UserMessage}
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object UserActor {
  // type-checked and by-value way to create Props
  // see https://blog.codecentric.de/en/2017/03/akka-best-practices-defining-actor-props/
  def props(daemon: ActorRef, repo: ChatroomRepo) =
    Props(new UserActor(daemon, repo))
}

class UserActor(daemon: ActorRef, repo: ChatroomRepo)
    extends Actor
    with ActorLogging
    with ActorLifecycleLogging {

  import Internal._

  private var _nextServerMessageSeq = -1
  private var outgoing: Option[ActorRef] = None
  private var userIdentity: Option[User] = None
  private var channelsJoining: Map[String, Int] = Map.empty
  private var channelsJoined: Map[String, ActorRef] = Map.empty
  private var messagesSending: Set[Int] = Set.empty
  private var clientKnownUsers: Set[User] = Set.empty
  private var channelMembers: Map[UUID, Set[User]] = Map.empty

  override def receive: Receive = wrapContext {
    case UserConnected(o) if outgoing.isEmpty =>
      log.info("user connected")
      this.outgoing = Some(o)
      context become connected
  }

  private def connected: Receive = wrapContext {
    // silly way to keep as a PartialFunction
    case UserMessage.Auth(seq, name, otp) if otp == "otp" =>
      val userInfo = repo.createUser(name)
      this.userIdentity = Some(userInfo)
      outgoing.get ! ServerMessage.Authed(seq, userInfo)
      daemon ! UserAuthed(userInfo)
      context become authed
  }

  private def authed: Receive = wrapContext {
    case UserMessage.JoinChannel(seq: Int, name: String) =>
      channelsJoining += name -> seq
      daemon ! JoinRequest(from = userIdentity.get, name, self)

    case UserMessage.LeaveChannel(seq, channelName)
        if channelsJoined.contains(channelName) =>
      daemon ! LeaveRequest(userIdentity.get, channelName)
      outgoing.get ! ServerMessage.LeftChannel(seq, "voluntarily")
      channelsJoined -= channelName

    case UserMessage.SendChatMessage(seq, channelName, text)
        if channelsJoined contains channelName =>
      val channelActor = channelsJoined(channelName)
      messagesSending += seq
      channelActor ! SendMessageRequest(
        from = userIdentity.get,
        channelName = channelName,
        text = text,
        seq = seq
      )

    case SendMessageResponse(seq, message)
        if messagesSending contains seq =>
      messagesSending -= seq
      outgoing.get ! ServerMessage.SentMessage(seq, message)

    case b @ ChannelBroadcast(channel, users, messages)
        if channelsJoining.contains(channel.name) =>
      // just joined a channel
      outgoing.get ! ServerMessage.JoinedChannel(
        channelsJoining(channel.name),
        channel,
        users.toSeq,
        messages
      )
      channelsJoining -= channel.name
      channelsJoined += channel.name -> sender
      clientKnownUsers ++= users
      channelMembers += channel.uuid -> users
      handleChannelBroadcast(b, sendMessages = false)

    case b @ ChannelBroadcast(channel, _, _)
        if channelsJoined.contains(channel.name) =>
      handleChannelBroadcast(b)
  }

  private def nextServerMessageSeq() = {
    _nextServerMessageSeq -= 1
    _nextServerMessageSeq
  }

  private def handleChannelBroadcast(b: ChannelBroadcast,
                                     sendMessages: Boolean = true): Unit = {
    val ChannelBroadcast(channel, users, newMessages) = b
    val newUsers = users -- clientKnownUsers

    outgoing.get ! ServerMessage.BroadCast(
      nextServerMessageSeq(),
      channels = List(
        ServerMessage.ChannelBroadcast(
          channel = channel,
          joinedUsers = (users -- channelMembers(channel.uuid)).toSeq,
          leftUsers = (channelMembers(channel.uuid) -- users).toSeq,
          newMessages =
            if (sendMessages) newMessages
            else Nil
        )
      ),
      newUsers = newUsers.toSeq
    )

    channelMembers += channel.uuid -> users
    clientKnownUsers ++= newUsers
  }

  private def hookBeforeReceive: Receive = {
    case UserDisconnected(None) =>
      log.debug("user disconnected, stopping")
      daemon ! UserDisconnected(userIdentity)
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
