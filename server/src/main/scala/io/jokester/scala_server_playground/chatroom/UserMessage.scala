package io.jokester.scala_server_playground.chatroom

import java.util.UUID

object UserMessage {

  sealed trait HaveSeq {
    def seq: Int
  }

  // for some reason we cannot match against FromUser without this trait
  abstract class FromUser(seq: Int) extends HaveSeq

  case class Ping(seq: Int) extends FromUser(seq)

  case class Auth(seq: Int, name: String, otp: String) extends FromUser(seq)

  case class JoinChannel(seq: Int, name: String) extends FromUser(seq)

  case class LeaveChannel(seq: Int, channelUuid: UUID) extends FromUser(seq)

  case class SendChatMessage(seq: Int, channelUuid: UUID, text: String) extends FromUser(seq)
}
