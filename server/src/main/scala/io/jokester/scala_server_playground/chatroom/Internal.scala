package io.jokester.scala_server_playground.chatroom

import java.time.Instant
import java.util.UUID

import akka.actor.ActorRef

object Internal {

  // ---------- models
  case class User(name: String, uuid: UUID, createdAt: Instant)

  case class Channel(name: String, uuid: UUID, createdAt: Instant)

  case class ChatMessage(uuid: UUID,
                         userUuid: UUID,
                         channelName: String,
                         text: String,
                         createdAt: Instant)

  // ---------- events
  case class UserConnected(outgoingActor: ActorRef)

  case class UserAuthed(user: User)

  case class UserDisconnected(user: Option[User])

  case class JoinRequest(from: User, channelName: String, userActor: ActorRef)

  case class LeaveRequest(from: User, channelName: String)

  case class SendMessageRequest(from: User, channelName: String, text: String, seq: Int)

  case class SendMessageResponse(seq: Int, message: ChatMessage)

  case class ChannelBroadcast(from: Channel,
                              users: Set[User],
                              newMessages: Seq[ChatMessage])

  // ----------- admin
  case class AdminQueryUserCount()

  case class AdminQueryUserCountRes(users: Seq[User],
                                    channels: Seq[Channel],
                                    channelUserCount: Map[String, Int])

}
