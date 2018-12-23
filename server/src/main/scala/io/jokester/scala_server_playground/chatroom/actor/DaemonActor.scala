package io.jokester.scala_server_playground.chatroom.actor

import akka.actor._
import io.jokester.scala_server_playground.util.{ ActorLifecycleLogging, Entropy }

object DaemonActor {
  def props(e: Entropy) = Props(new DaemonActor(e))
}

// singleton daemon: create chatrooms and guide user to requested chatroom
class DaemonActor(e: Entropy) extends Actor with ActorLogging with ActorLifecycleLogging {

  import io.jokester.scala_server_playground.chatroom.Internal._

  var channels: Map[String, ActorRef] = Map.empty

  override def receive: Receive = {
    case join @ JoinRequest(_, channel, _) =>
      channelOfName(channel) ! join
    //    case m =>
    //      log.warning("unhandled message: {}", m)
  }

  private def channelOfName(name: String): ActorRef = {
    if (channels.contains(name)) {
      channels(name)
    } else {
      val newChannel = context.actorOf(
        ChannelActor.props(e.nextUUID(), name),
        name = s"chatroom-$name")
      channels += name -> newChannel
      newChannel
    }
  }
}
