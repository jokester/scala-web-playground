package io.jokester.learning.scala_server.chat2

import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.Materializer
import io.circe
import io.circe.Json
import io.jokester.learning.scala_server.chat2.definitions._

import scala.concurrent.{ ExecutionContext, Future }

trait WsInterface {

  import UserMessage._
  import ServerMessage._
  import CommonStruct._

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

  def extractCmd(m: String): Either[Throwable, (Json, chat2Command)] = {
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
      }
    }
  }

  def encode(m: ServerMessage.ToUser): Message = {

    val json: Json = m match {
      case msg: ServerMessage.Pong => msg
      case msg: ServerMessage.Fail => msg
      case msg: ServerMessage.Authed => msg
    }

    TextMessage(json.toString())
  }

  private implicit def convert(msg: ServerMessage.Pong): Json = {
    ServerPong.encodeServerPong(
      ServerPong(
        chat2Command("Pong", msg.seq)))
  }

  private implicit def convert(msg: ServerMessage.Authed): Json = {
    ServerAuthed.encodeServerAuthed(
      ServerAuthed(
        chat2Command("Authed", msg.seq),
        msg.identity))
  }

  private implicit def convert(msg: ServerMessage.Fail): Json = {
    ServerBaseRes.encodeServerBaseRes(
      ServerBaseRes(
        chat2Command("Fail", msg.seq, Some(msg.errors.toIndexedSeq))))
  }

  private implicit def convert(userInfo: CommonStruct.UserInfo): chat2UserInfo = {
    chat2UserInfo(userInfo.name, userInfo.uuid.toString)
  }

}
