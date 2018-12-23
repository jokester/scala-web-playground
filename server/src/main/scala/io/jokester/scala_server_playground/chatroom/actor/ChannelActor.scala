package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object ChannelActor {
  def props(uuid: UUID, name: String) = Props(new ChannelActor(uuid, name))
}

class ChannelActor(uuid: UUID, name: String) extends Actor with ActorLogging with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  private var users = Map.empty[User, ActorRef]

  private val thisChannel = Channel(name, uuid)

  override def receive: Receive = {
    case JoinRequest(user, channel, userActor) if !users.contains(user) =>
      users += user -> userActor
      userActor ! ChannelBroadcast(thisChannel, users.keys.toSeq, Seq())
      logUsers()
    case LeaveRequest(user, channelUuid) if users.contains(user) && channelUuid == uuid =>
      users -= user
      logUsers()
  }

  private def logUsers(): Unit = {
    log.debug("{} users in channel {}", users.size, name)
  }
}