package io.jokester.scala_server_playground.chatroom

import java.util.{ Date, UUID }

import akka.actor.ActorRef

object CommonStruct {

  final case class UserInfo(name: String, uuid: UUID)

  final case class Channel(name: String, uuid: UUID, users: Seq[UserInfo])

  final case class ChatMessage(uuid: UUID, userUuid: UUID, channelUuid: UUID, text: String, timestamp: Date)

}

object UserMessage {

  trait HaveSeq {
    def seq: Int
  }

  // for some reason we cannot match against FromUser without this trait
  sealed abstract class FromUser(seq: Int) extends HaveSeq

  final case class Ping(seq: Int) extends FromUser(seq)

  final case class Auth(seq: Int, name: String, otp: String) extends FromUser(seq)

  //  case class JoinChannel(user: String, room: String, seqNo: Int) extends FromUser(seqNo)
  //
  //  case class LeaveRoom(user: String, room: String, seqNo: Int) extends FromUser(seqNo)
  //
  //  case class ChatMessage(user: String, room: String, message: String) extends FromUser

}

object ServerMessage {

  import CommonStruct._

  sealed abstract class ToUser(seq: Int)

  case class Pong(seq: Int) extends ToUser(seq)

  case class Fail(seq: Int, errors: Seq[String]) extends ToUser(seq)

  case class Authed(seq: Int, identity: UserInfo) extends ToUser(seq)

}

object ServerInternal {

  case class UserConnected(actor: ActorRef)

}