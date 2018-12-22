package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, Props }
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object ChatroomActor {
  def props(uuid: UUID) = Props(new ChatroomActor(uuid))
}

class ChatroomActor(uuid: UUID) extends Actor with ActorLogging with ActorLifecycleLogging {
  override def receive: Receive = {
    ???
  }
}