package io.jokester.scala_server_playground.chatroom

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
