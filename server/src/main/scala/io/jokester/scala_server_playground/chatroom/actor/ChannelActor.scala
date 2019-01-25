package io.jokester.scala_server_playground.chatroom.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import io.jokester.scala_server_playground.chatroom.ChatroomRepo
import io.jokester.scala_server_playground.chatroom.Internal._
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object ChannelActor {
  def props(channel: Channel, repo: ChatroomRepo) = Props(new ChannelActor(channel, repo))
  val InMemoryMessageCount = 1000
}

class ChannelActor(thisChannel: Channel, repo: ChatroomRepo)
    extends Actor
    with ActorLogging
    with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  private var users = Map.empty[User, ActorRef]
  private var userSet = Set.empty[User]
  var recentMessages: Seq[ChatMessage] = repo.listMessage(thisChannel.name, ChannelActor.InMemoryMessageCount)

  override def receive: Receive = {
    case JoinRequest(user, _, userActor) if !users.contains(user) =>
      users += user -> userActor
      userSet += user
      userActor ! ChannelBroadcast(
        thisChannel,
        userSet,
        recentMessages
      )
      onUsersChange()

    case LeaveRequest(user, channelName)
        if users.contains(user) && channelName == thisChannel.name =>
      removeUser(user)
      onUsersChange()

    case SendMessageRequest(user, channelName, text, seq)
        if users.contains(user) && channelName == thisChannel.name =>
      val chatMsg = repo.createMessage(user, thisChannel, text)
      sender ! SendMessageResponse(seq, chatMsg)
      broadcastMessage(chatMsg)

    case UserDisconnected(Some(user)) if userSet.contains(user) =>
      removeUser(user)
      onUsersChange()
  }

  private def removeUser(user: User): Unit = {
    users -= user
    userSet -= user
  }

  private def broadcastMessage(chatMsg: ChatMessage): Unit = {
    val broadcast = ChannelBroadcast(thisChannel, userSet, Seq(chatMsg))
    for ((_, userActor) <- users) {
      userActor ! broadcast
    }
    recentMessages = (recentMessages :+ chatMsg).takeRight(ChannelActor.InMemoryMessageCount)
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
