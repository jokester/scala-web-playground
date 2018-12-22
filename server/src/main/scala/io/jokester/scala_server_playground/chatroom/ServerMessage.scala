package io.jokester.scala_server_playground.chatroom

object ServerMessage {

  import Internal._

  sealed abstract class ToUser(seq: Int)

  case class Pong(seq: Int) extends ToUser(seq)

  case class Fail(seq: Int, errors: Seq[String]) extends ToUser(seq)

  case class Authed(seq: Int, identity: UserInfo) extends ToUser(seq)

  case class JoinedChannel(seq: Int, channel: Channel) extends ToUser(seq)
}
