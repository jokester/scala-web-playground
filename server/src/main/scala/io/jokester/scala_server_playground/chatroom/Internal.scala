package io.jokester.scala_server_playground.chatroom

import java.util.{ Date, UUID }

import akka.actor.ActorRef

object Internal {

  case class UserConnected(actor: ActorRef)

  final case class UserInfo(name: String, uuid: UUID)

  final case class Channel(name: String, uuid: UUID, users: Seq[UserInfo])

  final case class ChatMessage(uuid: UUID, userUuid: UUID, channelUuid: UUID, text: String, timestamp: Date)

}