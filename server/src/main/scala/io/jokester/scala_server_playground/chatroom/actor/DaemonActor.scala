package io.jokester.scala_server_playground.chatroom.actor

import akka.actor._
import io.jokester.scala_server_playground.util.{ ActorLifecycleLogging, Entropy }

object DaemonActor {
  def props(e: Entropy) = Props(new DaemonActor(e))
}

/**
  *
  * singleton daemon:
  * - create channels and
  * - forward join request to the channel
  * - notice channels after user disconnected
  *
  * @param e
  */
class DaemonActor(e: Entropy) extends Actor with ActorLogging with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  var authedUsers: Map[User, List[String]] = Map.empty
  var channelName2Actor: Map[String, ActorRef] = Map.empty

  override def receive: Receive = {

    case UserAuthed(user) if !authedUsers.contains(user) =>
      authedUsers += user -> Nil

    case join @ JoinRequest(user, channel, _) if authedUsers.contains(user) =>
      val c = channelOfName(channel)
      authedUsers += user -> (channel :: authedUsers(user))
      c ! join

    case d @ UserDisconnected(userUuid) =>
      for (
        user: User <- authedUsers.keys.find(u => u.uuid == userUuid)
      ) {
        val channelNames = authedUsers(user)
        authedUsers -= user
        for (
          channelName <- channelNames;
          channelActor <- channelName2Actor.get(channelName)
        ) {
          channelActor ! d
        }
      }

    //    case m =>
    //      log.warning("unhandled message: {}", m)
  }

  private def channelOfName(name: String): ActorRef = {
    if (channelName2Actor.contains(name)) {
      channelName2Actor(name)
    } else {
      val newChannel = context.actorOf(
        ChannelActor.props(e.nextUUID(), name),
        name = s"ChannelActor-$name")
      channelName2Actor += name -> newChannel
      newChannel
    }
  }

}
