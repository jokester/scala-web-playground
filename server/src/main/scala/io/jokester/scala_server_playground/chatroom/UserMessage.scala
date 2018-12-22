package io.jokester.scala_server_playground.chatroom

object UserMessage {

  trait HaveSeq {
    def seq: Int
  }

  // for some reason we cannot match against FromUser without this trait
  sealed abstract class FromUser(seq: Int) extends HaveSeq

  case class Ping(seq: Int) extends FromUser(seq)

  case class Auth(seq: Int, name: String, otp: String) extends FromUser(seq)

  case class JoinChannel(seq: Int, name: String) extends FromUser(seq)
  //
  //  case class LeaveChannel(user: String, room: String, seq: Int) extends FromUser(seq)
  //
  //  case class ChatMessage(user: String, room: String, message: String, seq: Int) extends FromUser(seq)

}
