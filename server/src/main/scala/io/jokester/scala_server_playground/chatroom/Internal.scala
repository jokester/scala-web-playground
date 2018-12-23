package io.jokester.scala_server_playground.chatroom

import java.time.ZonedDateTime
import java.util.UUID

import akka.actor.ActorRef

object Internal {

  // ---------- models
  case class User(name: String, uuid: UUID)

  case class Channel(name: String, uuid: UUID)

  case class ChatMessage(uuid: UUID, userUuid: UUID, channelUuid: UUID, text: String, timestamp: ZonedDateTime)

  // ---------- events
  case class UserConnected(outgoingActor: ActorRef)

  case class UserAuthed(user: User)

  case class UserDisconnected(userUuid: UUID)

  case class JoinRequest(from: User, channel: String, userActor: ActorRef)

  case class LeaveRequest(from: User, channelUuid: UUID)

  case class SendMessageRequest(from: User, message: ChatMessage)

  case class SendMessageResponse(messageUuid: UUID)

  case class ChannelBroadcast(from: Channel, users: Set[User], newMessages: Seq[ChatMessage])

  // ----------- admin
  case class AdminQueryUserCount()

  case class AdminQueryUserCountRes(users: Seq[User], channels: Seq[Channel], channelUserCount: Map[String, Int])

}