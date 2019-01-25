package io.jokester.scala_server_playground.chatroom

import java.time.Instant
import java.util.UUID

import io.jokester.scala_server_playground.chatroom.Internal.{Channel => IChannel, ChatMessage => IChatMessage, User => IUser}
import io.jokester.scala_server_playground.util.Entropy
import scalikejdbc._

object ChatroomPersistRepo {

  object User extends SQLSyntaxSupport[IUser] {
    override def tableName = "chatroom_user"

    def apply(g: ResultName[IUser])(rs: WrappedResultSet): IUser = {
      Internal.User(
        name = rs.string(g.name),
        uuid = UUID.fromString(rs.string(g.uuid)),
        createdAt = rs.offsetDateTime(g.createdAt).toInstant
      )
    }
  }

  object Channel extends SQLSyntaxSupport[IChannel] {
    override def tableName = "chatroom_channel"
    def apply(g: ResultName[IChannel])(rs: WrappedResultSet): IChannel = {
      IChannel(
        name = rs.string(g.name),
        uuid = UUID.fromString(rs.string(g.uuid)),
        createdAt = rs.offsetDateTime(g.createdAt).toInstant
      )
    }
  }

  object ChatMessage extends SQLSyntaxSupport[IChatMessage] {
    override def tableName = "chatroom_chat_message"
    def apply(
      g: ResultName[IChatMessage]
    )(rs: WrappedResultSet): IChatMessage = {
      IChatMessage(
        uuid = UUID.fromString(rs.string(g.uuid)),
        userUuid = UUID.fromString(rs.string(g.userUuid)),
        channelName = rs.string(g.channelName),
        text = rs.string(g.text),
        createdAt = rs.offsetDateTime(g.createdAt).toInstant
      )
    }
  }

}

class ChatroomPersistRepo(getDB: () => DB)(implicit e: Entropy)
    extends ChatroomRepo {

  import ChatroomPersistRepo.{ChatMessage, User}
  import io.jokester.scala_server_playground.util.UUID4pg.uuidBinder
  private val u = User.syntax("u")
  private val uc = User.column

  private val m = ChatMessage.syntax("m")
  private val mc = ChatMessage.column

  override def createUser(name: String): IUser = {
    val user =
      IUser(name = name, uuid = e.nextUUID(), createdAt = e.currentServerTime())

    getDB() localTx { implicit session =>
      applyUpdate {
        insertInto(User).namedValues(
          uc.name -> user.name,
          uc.uuid -> user.uuid,
          uc.createdAt -> user.createdAt
        )
      }
    }

    user
  }

  override def createChannel(name: String): Internal.Channel = {
    IChannel(
      name = name,
      uuid = e.nextUUID(),
      createdAt = e.currentServerTime()
    )
  }

  override def createMessage(user: Internal.User,
                             channel: Internal.Channel,
                             text: String): Internal.ChatMessage = {
    val message = IChatMessage(
      uuid = e.nextUUID(),
      userUuid = user.uuid,
      channelName = channel.name,
      text = text,
      createdAt = e.currentServerTime()
    )

    getDB() localTx { implicit session =>
      applyUpdate {
        insertInto(ChatMessage).namedValues(
          mc.uuid -> message.uuid,
          mc.channelName -> message.channelName,
          mc.userUuid -> message.userUuid,
          mc.text -> message.text,
          mc.createdAt -> message.createdAt
        )
      }
    }

    message
  }

  override def findUser(uuid: UUID): Option[Internal.User] = {
    getDB() readOnly { implicit session =>
      sql"SELECT ${u.result.*} FROM ${User as u} WHERE ${u.uuid} = $uuid"
        .map(User(u.resultName))
        .single()
        .apply()
    }
  }

  override def listChannel(): Seq[String] = {
    getDB() readOnly { implicit session =>
      sql"SELECT ${m.result.channelName} from ${ChatMessage as m} DISTINCT"
        .map(r => r.string(0))
        .list()
        .apply()
    }
  }

  override def listMessage(channelName: String, limit: Int = 500): Seq[Internal.ChatMessage] = {
    getDB() readOnly { implicit s =>
      sql"SELECT ${m.result.*} FROM ${ChatMessage as m} WHERE ${m.channelName} = $channelName ORDER BY ${m.createdAt} DESC LIMIT $limit"
        .map(ChatMessage(m.resultName))
        .list()
        .apply()
        .reverse
    }
  }

  override def countObject(priorTo: Instant): Int = {
    getDB() readOnly { implicit s =>
      val userCount = sql"SELECT COUNT(*) FROM ${User as u}"
        .map(r => r.int(0))
        .single()
        .apply()
        .get

      val messageCount = sql"SELECT COUNT(*) FROM ${ChatMessage as m}"
        .map(r => r.int(0))
        .single()
        .apply()
        .get

      userCount + messageCount
    }
  }

  override def cleanObject(priorTo: Instant): Int = {
    getDB() localTx { implicit session =>
      val c1 = sql"DELETE FROM ${User as u} WHERE ${u.createdAt} < $priorTo"
        .update()
        .apply()

      val c2 = sql"DELETE FROM ${ChatMessage as m} WHERE ${m.createdAt} < $priorTo"
        .update()
        .apply()

      c1 + c2
    }
  }
}
