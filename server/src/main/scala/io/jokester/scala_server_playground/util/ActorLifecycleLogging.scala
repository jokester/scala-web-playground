package io.jokester.scala_server_playground.util

trait ActorLifecycleLogging extends akka.actor.ActorLogging {
  this: akka.actor.Actor =>

  override def preStart(): Unit =
    log.debug("actor {} ({}) starting", psyPath, getClass)

  override def postStop(): Unit =
    log.debug("actor {} ({}) stopped", psyPath, getClass)

  private def psyPath: String =
    akka.serialization.Serialization.serializedActorPath(self)
}
