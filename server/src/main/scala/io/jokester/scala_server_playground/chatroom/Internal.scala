package io.jokester.scala_server_playground.chatroom

import java.util.{ Date, UUID }

import akka.actor.ActorRef

object Internal {

  case class UserConnected(outgoingActor: ActorRef)

  case class UserDisconnected(userUuid: UUID)

  case class User(name: String, uuid: UUID)

  case class Channel(name: String, uuid: UUID)

  case class ChatMessage(uuid: UUID, userUuid: UUID, channelUuid: UUID, text: String, timestamp: Date)

  case class JoinRequest(from: User, channel: String, userActor: ActorRef)

  case class ChannelBroadcast(from: Channel, users: Seq[User], messages: Seq[ChatMessage])

}