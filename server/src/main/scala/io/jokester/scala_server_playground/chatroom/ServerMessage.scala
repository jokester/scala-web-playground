package io.jokester.scala_server_playground.chatroom

object ServerMessage {

  import Internal._

  sealed abstract class ToUser(seq: Int)

  case class Pong(seq: Int) extends ToUser(seq)

  case class Fail(seq: Int, errors: Seq[String]) extends ToUser(seq)

  case class Authed(seq: Int, identity: User) extends ToUser(seq)

  case class JoinedChannel(seq: Int, channel: Channel, users: Seq[User], messages: Seq[ChatMessage])
    extends ToUser(seq)

  case class BroadCast(seq: Int, users: Seq[User], channels: Seq[Channel], messages: Seq[ChatMessage])

}
