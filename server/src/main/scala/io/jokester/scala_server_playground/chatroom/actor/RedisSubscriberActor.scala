package io.jokester.scala_server_playground.chatroom.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.redis._
import io.jokester.scala_server_playground.chatroom.actor.RedisSubscriberActor.SubscriptionState

object RedisSubscriber {

  // register sender
  case class Subscribe(channels: Seq[String])

  // unregister sender
  case class Unsubscribe(channels: Seq[String])

  // unregister caller from *all*
  case class UnsubscribeAll()

  case class Published(channel: String, data: String)

}

object RedisSubscriberActor {

  def props(redisClient: RedisClient) =
    Props(new RedisSubscriberActor(redisClient))

  case class SubscriptionState(subscriber: Map[String, Set[ActorRef]],
                               subscribed: Map[ActorRef, Set[String]]) {
    def onSubscribe(actor: ActorRef, channels: String*): SubscriptionState = {

      val newSubscribed = subscribed.updated(
        actor,
        subscribed.getOrElse(actor, Set.empty) ++ channels
      )
      val newSubscriber = channels.foldLeft(subscriber) { (s, channel) =>
        s.updated(channel, s.getOrElse(channel, Set.empty) + actor)
      }

      SubscriptionState(subscriber = newSubscriber, subscribed = newSubscribed)
    }

    def onUnsubscribe(actor: ActorRef, channels: String*): SubscriptionState = {

      val newSubscribed = subscribed.updated(
        actor,
        subscribed.getOrElse(actor, Set.empty) -- channels
      )
      val newSubscriber = channels.foldLeft(subscriber) { (s, channel) =>
        s.updated(channel, s.getOrElse(channel, Set.empty) - actor)
      }
      SubscriptionState(subscriber = newSubscriber, subscribed = newSubscribed)
    }

    def onUnsubscribeAll(actor: ActorRef): SubscriptionState = {
      val wasSubscribed = subscribed.getOrElse(actor, Set.empty)
      val newSubscribed = subscribed - actor
      val newSubscriber = wasSubscribed.foldLeft(subscriber) { (s, channel) =>
        val x = s.getOrElse(channel, Set.empty) - actor
        if (x.isEmpty) s - channel else s.updated(channel, x)
      }
      SubscriptionState(subscriber = newSubscriber, subscribed = newSubscribed)
    }

    def numSubscriber(channel: String): Int = {
      subscriber.getOrElse(channel, Set.empty).size
    }
  }

}

/**
  * Actor that multiplexes redis subscription and forwards channel to registered actor
  *
  * (there is no 'publisher' actor, each actor can borrow a RedisClient to publish)
  */
class RedisSubscriberActor(redisClient: RedisClient)
    extends Actor
    with ActorLogging {

  import RedisSubscriber._

  var state = SubscriptionState(subscriber = Map.empty, subscribed = Map.empty)

  override def receive: Receive = {
    // from other actor
    case Subscribe(channels) =>
      state = state.onSubscribe(sender, channels: _*)
      val me = self
      redisClient.subscribe(channels.head, channels.tail: _*) {
        msg: PubSubMessage =>
          // from com.redis.PubSub.Consumer thread
          me ! msg
      }

    case Unsubscribe(channels) =>
      val oldState = state
      state = state.onUnsubscribe(sender, channels: _*)
      val toUnsubscribe = channels.filter(
        c => oldState.numSubscriber(c) > 0 && state.numSubscriber(c) == 0
      )
      if (toUnsubscribe.nonEmpty) {
        redisClient.unsubscribe(toUnsubscribe.head, toUnsubscribe.tail: _*)
      }

    case UnsubscribeAll() =>
      val wasSubscribing = state.subscribed.getOrElse(sender, Set.empty).toSeq
      state = state.onUnsubscribeAll(sender)
      val toUnsubscribe =
        wasSubscribing.filter(c => state.numSubscriber(c) == 0)
      if (toUnsubscribe.nonEmpty) {
        redisClient.unsubscribe(toUnsubscribe.head, toUnsubscribe.tail: _*)
      }

    // from com.redis
    case S(channel, count) =>
      log.info("subscribed to {} / subscribing to {}", channel, count)

    case U(channel, count) =>
      log.info("unsubscribed to {} / subscribing to {}", channel, count)

    case M(channel, data) =>
      val p = Published(channel, data)
      for (receiver <- state.subscriber.getOrElse(channel, Set.empty)) {
        receiver ! p
      }
    // NOT handling com.redis.redisclient.E
  }
}
