package io.jokester.scala_server_playground.chatroom.actor

import java.util.UUID

import akka.actor._
import io.jokester.scala_server_playground.util.{ ActorLifecycleLogging, Entropy }

object DaemonActor {
  def props(e: Entropy) = Props(new DaemonActor(e))
}

/**
 *
 * singleton daemon:
 * - create channels
 * - forward join/leave request to the channel
 * - shutdown channel after all user leaves
 *
 * @param e
 */
class DaemonActor(e: Entropy) extends Actor with ActorLogging with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  type ExistingChannel = (Channel, ActorRef)

  var authedUsers: Map[User, Set[String]] = Map.empty
  var channels: Map[UUID, ExistingChannel] = Map.empty
  var channelName2uuid: Map[String, UUID] = Map.empty

  override def receive: Receive = {

    case UserAuthed(user) if !authedUsers.contains(user) =>
      authedUsers += user -> Set.empty

    case join @ JoinRequest(user, channelName, _) if authedUsers.contains(user) =>
      val (_, a) = channelOfName(channelName)
      a ! join
      authedUsers += user -> (authedUsers(user) + channelName)

    case leave @ LeaveRequest(user, channelUuid) if authedUsers.contains(user) && channels.contains(channelUuid) =>
      val (channel, a) = channels(channelUuid)
      authedUsers += user -> (authedUsers(user) - channel.name)
      a ! leave
      shutdownEmptyChannel()

    case d @ UserDisconnected(userUuid) =>
      for (
        user: User <- authedUsers.keys.find(u => u.uuid == userUuid)
      ) {
        val channelNames = authedUsers(user)
        authedUsers -= user
        for (
          channelName <- channelNames;
          channelUuid <- channelName2uuid.get(channelName);
          (_, a) <- channels.get(channelUuid)
        ) {
          a ! d
        }
      }
      shutdownEmptyChannel()

    case AdminQueryUserCount() =>
      sender ! AdminQueryUserCountRes(
        authedUsers.keys.toSeq,
        channels.values.map(_._1).toSeq,
        countChannelUsers())

    //    case m =>
    //      log.warning("unhandled message: {}", m)
  }

  private def countChannelUsers(): Map[String, Int] =
    authedUsers.values.flatten.foldLeft[Map[String, Int]](Map.empty) { (count, channelName) =>
      count.updated(
        channelName,
        1 + count.getOrElse(channelName, 0))
    }

  private def shutdownEmptyChannel(): Unit = {
    val userCount = countChannelUsers()
    for (emptyChannelName <- channelName2uuid.keys.filterNot(userCount contains)) {
      val channelUuid = channelName2uuid(emptyChannelName)
      context.stop(channels(channelUuid)._2)
      channels -= channelUuid
      channelName2uuid -= emptyChannelName
    }
  }

  private def channelOfName(name: String): ExistingChannel = {
    if (channelName2uuid.contains(name)) {
      channels(channelName2uuid(name))
    } else {
      val channel = Channel(name, uuid = e.nextUUID())
      val channelActor = context.actorOf(
        ChannelActor.props(channel),
        name = s"ChannelActor-$name")
      val c = (channel, channelActor)
      channels += channel.uuid -> c
      channelName2uuid += name -> channel.uuid
      c
    }
  }

}
