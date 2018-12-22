package io.jokester.scala_server_playground.chatroom

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.Materializer
import io.circe
import io.circe.Json
import io.jokester.scala_server_playground.chatroom.definitions._

import scala.concurrent.{ExecutionContext, Future}

trait WsInterface {

  def retrieveCompleteMessage(msg: Message)(implicit m: Materializer): Future[TextMessage.Strict] = {
    implicit val ctx: ExecutionContext = m.executionContext
    msg match {
      case m: TextMessage.Strict =>
        Future.successful(m)
      case TextMessage.Streamed(stream) =>
        stream.runFold[StringBuilder](StringBuilder.newBuilder)((builder, part) => builder.append(part))
          .map(builder => builder.toString())
          .map(text => TextMessage.Strict(text))
      case _ => Future.failed(new IllegalArgumentException(s"cannot retrieve message: $m"))
    }
  }

  def extractCmd(m: String): Either[Throwable, (Json, chatroomCommand)] = {
    import circe.parser._
    for (
      json <- parse(m);
      msg <- UserBaseReq.decodeUserBaseReq.decodeJson(json)
    ) yield (json, msg.cmd)
  }

  def decode(m: String): Either[Throwable, UserMessage.FromUser] = {
    extractCmd(m).flatMap { t =>
      val (json, cmd) = t

      cmd.method match {
        case n if n == "Ping" =>
          UserPing.decodeUserPing.decodeJson(json)
            .map(ping => UserMessage.Ping(ping.cmd.seq))
        case n if n == "Auth" =>
          UserAuth.decodeUserAuth.decodeJson(json)
            .map(auth => UserMessage.Auth(auth.cmd.seq, name = auth.name, otp = auth.otp))
        case n if n == "JoinChannel" =>
          UserJoinChannel.decodeUserJoinChannel.decodeJson(json)
            .map(m => UserMessage.JoinChannel(m.cmd.seq, name = m.name))
      }
    }
  }

  def encode(m: ServerMessage.ToUser): Message = {

    val json: Json = m match {
      case msg: ServerMessage.Pong => msg
      case msg: ServerMessage.Fail => msg
      case msg: ServerMessage.Authed => msg
      case msg: ServerMessage.JoinedChannel => msg
    }

    TextMessage(json.toString())
  }

  private implicit def convert(msg: ServerMessage.Pong): Json = {
    ServerPong.encodeServerPong(
      ServerPong(
        chatroomCommand("Pong", msg.seq)))
  }

  private implicit def convert(msg: ServerMessage.Authed): Json = {
    ServerAuthed.encodeServerAuthed(
      ServerAuthed(
        chatroomCommand("Authed", msg.seq),
        msg.identity))
  }

  private implicit def convert(msg: ServerMessage.Fail): Json = {
    ServerBaseRes.encodeServerBaseRes(
      ServerBaseRes(
        chatroomCommand("Fail", msg.seq, Some(msg.errors.toIndexedSeq))))
  }

  private implicit def convert(joined: ServerMessage.JoinedChannel): Json = {
    ServerJoinedChannel.encodeServerJoinedChannel(
      ServerJoinedChannel(
        chatroomCommand("JoinedChannel", joined.seq),
        channel = joined.channel,
        users = joined.users.toIndexedSeq.map(convert),
        history = joined.history.toIndexedSeq.map(convert)
      ),
    )
  }

  private implicit def convert(userInfo: Internal.User): chatroomUserInfo = {
    chatroomUserInfo(userInfo.name, userInfo.uuid.toString)
  }

  private implicit def convert(channel: Internal.Channel): chatroomChannelInfo = {
    chatroomChannelInfo(
      name = channel.name,
      uuid = channel.uuid.toString)
  }


  private implicit def convert(chatMessage: Internal.ChatMessage): chatroomChatMessage =
    chatroomChatMessage(
      uuid = chatMessage.uuid.toString,
      userUuid = chatMessage.userUuid.toString,
      channelUuid = chatMessage.channelUuid.toString,
      text = chatMessage.text,
      timestamp = chatMessage.timestamp.toString
    )
}
