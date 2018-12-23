package io.jokester.scala_server_playground.util

import java.time.{ Clock, LocalDateTime, ZonedDateTime }
import java.util.{ Calendar, Date, Random, UUID }

trait Entropy {
  def nextUUID(): UUID
  def currentServerTime(): ZonedDateTime
  def currentTimestampInMS(): Long = {
    val now = currentServerTime()
    now.toEpochSecond * 1000 + now.getNano / 1000000
  }
}

class ControlledEntropy(var r: Random, var currentServerTime: ZonedDateTime = RealWorld.currentServerTime()) extends Entropy {
  override def nextUUID(): UUID = new UUID(r.nextLong(), r.nextLong())
}

object RealWorld extends Entropy {
  override def nextUUID(): UUID = UUID.randomUUID()

  private val utcClock = Clock.systemUTC()

  override def currentServerTime(): ZonedDateTime = ZonedDateTime.now(utcClock)
}
