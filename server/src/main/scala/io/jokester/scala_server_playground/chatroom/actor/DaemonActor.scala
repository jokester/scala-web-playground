package io.jokester.scala_server_playground.chatroom.actor

import akka.actor._
import com.redis.RedisClient
import io.jokester.scala_server_playground.chatroom.ChatroomRepo
import io.jokester.scala_server_playground.conf.ServerConf
import io.jokester.scala_server_playground.util.ActorLifecycleLogging

object DaemonActor {
  def props(repo: ChatroomRepo) = Props(new DaemonActor(repo))
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
class DaemonActor(repo: ChatroomRepo)
    extends Actor
    with ActorLogging
    with ActorLifecycleLogging {

  val subscriberActor: ActorRef = {
    val conf = ServerConf.redisConf.get
    val client = new RedisClient(host = conf.host, port = conf.port)
    val actor = context.actorOf(RedisSubscriberActor.props(client))
    actor
  }

  import io.jokester.scala_server_playground.chatroom.Internal._

  type ExistingChannel = (Channel, ActorRef)

  var authedUsers: Map[User, Set[String]] = Map.empty
  var channels: Map[String, ExistingChannel] = Map.empty

  override def receive: Receive = {

    case UserAuthed(user) if !authedUsers.contains(user) =>
      authedUsers += user -> Set.empty

    case join @ JoinRequest(user, channelName, _)
        if authedUsers.contains(user) =>
      val (_, a) = channelOfName(channelName)
      a ! join
      authedUsers += user -> (authedUsers(user) + channelName)

    case leave @ LeaveRequest(user, channelName)
        if authedUsers.contains(user) && channels.contains(channelName) =>
      val (channel, a) = channels(channelName)
      authedUsers += user -> (authedUsers(user) - channel.name)
      a ! leave
      shutdownEmptyChannel()

    case d @ UserDisconnected(Some(user)) if authedUsers.contains(user) =>
      val channelNames = authedUsers(user)
      authedUsers -= user
      for (channelName <- channelNames;
           (_, a) <- channels.get(channelName)) {
        a ! d
      }
      shutdownEmptyChannel()

    case AdminQueryUserCount() =>
      sender ! AdminQueryUserCountRes(
        authedUsers.keys.toSeq,
        channels.values.map(_._1).toSeq,
        countChannelUsers()
      )

    //    case m =>
    //      log.warning("unhandled message: {}", m)
  }

  private def countChannelUsers(): Map[String, Int] =
    authedUsers.values.flatten.foldLeft[Map[String, Int]](Map.empty) {
      (count, channelName) =>
        count.updated(channelName, 1 + count.getOrElse(channelName, 0))
    }

  private def shutdownEmptyChannel(): Unit = {
    val userCount = countChannelUsers()
    for (emptyChannelName <- channels.keys.filterNot(userCount contains)) {
      val (_, channelActor) = channels(emptyChannelName)
      channels -= emptyChannelName

      // not sending poisonpill: channel actor should stop itself
      // context.system.scheduler.scheduleOnce(5 seconds, channelActor, PoisonPill)(context.dispatcher)
    }
  }

  private def channelOfName(name: String): ExistingChannel = {
    if (channels.contains(name)) {
      channels(name)
    } else {
      val channel = repo.createChannel(name)
      val channelActor = context.actorOf(
        ChannelActor.props(channel, repo),
        name = s"ChannelActor-$name-${channel.uuid}"
      )
      val c = (channel, channelActor)
      channels += channel.name -> c
      c
    }
  }

}
