package io.jokester.scala_server_playground.chatroom

import java.time.Instant
import java.util.UUID

trait ChatroomRepo {

  import Internal.{Channel, ChatMessage, User}

  def createUser(name: String): User

  def createChannel(name: String): Channel

  def createMessage(user: User, channel: Channel, text: String): ChatMessage

  def findUser(uuid: UUID): Option[User]

  def listChannel(): Seq[String]

  def listMessage(channelName: String, limit: Int = 500): Seq[ChatMessage]

  def countObject(priorTo: Instant): Int

  def cleanObject(priorTo: Instant): Int
}
