package io.jokester.scala_server_playground.chatroom.actor

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import io.jokester.scala_server_playground.chatroom.Internal._
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object ChannelActor {
  def props(channel: Channel) = Props(new ChannelActor(channel))
}

class ChannelActor(thisChannel: Channel) extends Actor with ActorLogging with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  private var users = Map.empty[User, ActorRef]
  private var userSet = Set.empty[User]

  override def receive: Receive = {
    case JoinRequest(user, _, userActor) if !users.contains(user) =>
      users += user -> userActor
      userSet += user
      userActor ! ChannelBroadcast(thisChannel, userSet, /* FIXME: recent messages */ Seq())
      onUsersChange()

    case LeaveRequest(user, channelUuid) if users.contains(user) && channelUuid == thisChannel.uuid =>
      removeUser(user)
      onUsersChange()

    case SendMessageRequest(user, chatMsg) if users.contains(user) && chatMsg.channelUuid == thisChannel.uuid =>
      // TODO: persist message?
      sender ! SendMessageResponse(chatMsg.uuid)
      val broadcast = ChannelBroadcast(thisChannel, userSet, Seq(chatMsg))
      for ((_, userActor) <- users) {
        userActor ! broadcast
      }

    case UserDisconnected(userUuid) =>
      for (user <- userSet.find(u => u.uuid == userUuid)) {
        removeUser(user)
      }
      onUsersChange()
  }

  private def removeUser(user: User): Unit = {
    users -= user
    userSet -= user
  }

  private def onUsersChange(): Unit = {
    log.debug("{} users in channel {}", users.size, thisChannel.name)
    val broadcast = ChannelBroadcast(thisChannel, userSet, Nil)
    for ((_, userActor) <- users) {
      userActor ! broadcast
    }

    if (users.isEmpty) {
      context.stop(self)
    }
  }
}