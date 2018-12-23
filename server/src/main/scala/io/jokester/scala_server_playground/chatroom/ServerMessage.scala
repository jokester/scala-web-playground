package io.jokester.scala_server_playground.chatroom

import java.util.UUID

object ServerMessage {

  import Internal._

  sealed abstract class ToUser(seq: Int)

  case class Pong(seq: Int) extends ToUser(seq)

  case class Fail(seq: Int, errors: Seq[String]) extends ToUser(seq)

  case class Authed(seq: Int, identity: User) extends ToUser(seq)

  case class JoinedChannel(seq: Int, channel: Channel, users: Seq[User], messages: Seq[ChatMessage])
    extends ToUser(seq)

  case class LeftChannel(seq: Int, reason: String) extends ToUser(seq)

  case class SentMessage(seq: Int, msg: ChatMessage) extends ToUser(seq)

  case class ChannelBroadcast(channel: Channel, joinedUsers: Seq[User], leftUsers: Seq[User], newMessages: Seq[ChatMessage])

  case class BroadCast(seq: Int, channels: Seq[ChannelBroadcast], newUsers: Seq[User]) extends ToUser(seq)

}
