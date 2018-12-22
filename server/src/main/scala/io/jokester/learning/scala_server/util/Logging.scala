package io.jokester.learning.scala_server.util

import com.typesafe.scalalogging.Logger

@Deprecated
trait Logging {
  // FIXME: should be private
  protected lazy val logger = Logger(getClass)
  protected def logWarnT(msg: => String, reason: Throwable): Unit = logger.warn(msg, reason)

  protected def logWarn(format: String, args: Any*): Unit = logger.warn(format, args)
  protected def logDebugT(msg: => String, reason: Throwable): Unit = logger.debug(msg, reason)

  protected def logDebug(format: String, arg1: Any*): Unit = logger.debug(format, arg1)
}

trait ActorLoggingLifecycle extends akka.actor.ActorLogging {
  this: akka.actor.Actor =>

  override def preStart(): Unit = log.debug("actor {} ({}) starting", psyPath, getClass)

  override def postStop(): Unit = log.debug("actor {} ({}) stopped", psyPath, getClass)

  private def psyPath: String = akka.serialization.Serialization.serializedActorPath(self)
}