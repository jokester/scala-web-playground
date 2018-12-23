package io.jokester.scala_server_playground.chatroom.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import io.jokester.scala_server_playground.util.ActorLifecycleLogging
import io.jokester.scala_server_playground.chatroom.Internal._

object ChannelActor {
  def props(channel: Channel) = Props(new ChannelActor(channel))
}

class ChannelActor(thisChannel: Channel) extends Actor with ActorLogging with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  private var users = Map.empty[User, ActorRef]

  override def receive: Receive = {
    case JoinRequest(user, _, userActor) if !users.contains(user) =>
      users += user -> userActor
      userActor ! ChannelBroadcast(thisChannel, users.keys.toSeq, Seq())
      logUsers()
    case LeaveRequest(user, channelUuid) if users.contains(user) && channelUuid == thisChannel.uuid =>
      users -= user
      logUsers()
    case SendMessageRequest(user, chatMsg) if users.contains(user) && chatMsg.channelUuid == thisChannel.uuid =>
      sender ! SendMessageResponse(chatMsg.uuid)
    // TODO: broadcast message
  }

  private def logUsers(): Unit = {
    log.debug("{} users in channel {}", users.size, thisChannel.name)
  }
}