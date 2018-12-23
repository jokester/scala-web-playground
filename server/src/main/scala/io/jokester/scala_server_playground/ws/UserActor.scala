package io.jokester.scala_server_playground.ws

import akka.actor._
import akka.http.scaladsl.model.ws.TextMessage

object UserActor {
  def props() = Props(new UserActor)

  case class OutgoingActor(actor: ActorRef)

  case object ConnectionClose

}

class UserActor extends Actor with ActorLogging {

  import UserActor._

  var outgoing: Option[ActorRef] = None

  override def aroundReceive(receive: Receive, msg: Any): Unit = {
    log.debug("aroundReceive: {}", msg)
    super.aroundReceive(receive, msg)
  }

  override def aroundPreStart(): Unit = {
    log.debug("aroundPreStart: {}", self)
    super.aroundPreStart()
  }

  override def aroundPostStop(): Unit = {
    log.debug("aroundPostStop: {}", self)
    super.aroundPostStop()
  }

  override def receive: Receive = {
    case OutgoingActor(outgoingActor) =>
      outgoing = Some(outgoingActor)
    case ConnectionClose =>
      context.stop(self)
    case tm: TextMessage if tm.isStrict =>
      outgoing.get ! TextMessage(s"you said ${tm.getStrictText}")
  }

  override def preStart(): Unit = {
    super.preStart()
    log.debug("preStart: {}", self)

  }

  override def postStop(): Unit = {
    super.postStop()
    log.debug("postStop: {}", self)
  }
}
