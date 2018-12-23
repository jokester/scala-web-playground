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
  var channelName2Actor: Map[String, (Channel, ActorRef)] = Map.empty

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
          (_, channelActor) <- channelName2Actor.get(channelName)
        ) {
          channelActor ! d
        }
      }

    case AdminQueryUserCount() =>
      val users = authedUsers.keys.toSeq
      val channels = channelName2Actor.values.map(_._1).toSeq
      val channelUserCount = authedUsers.values.flatten.foldLeft[Map[String, Int]](Map.empty) { (count, channelName) =>
        count.updated(
          channelName,
          1 + count.getOrElse(channelName, 0))
      }
      sender ! AdminQueryUserCountRes(users, channels, channelUserCount)

    //    case m =>
    //      log.warning("unhandled message: {}", m)
  }

  private def channelOfName(name: String): ActorRef = {
    if (channelName2Actor.contains(name)) {
      channelName2Actor(name)._2
    } else {
      val channel = Channel(name, uuid = e.nextUUID())
      val channelActor = context.actorOf(
        ChannelActor.props(channel),
        name = s"ChannelActor-$name")
      channelName2Actor += name -> (channel, channelActor)
      channelActor
    }
  }

}
