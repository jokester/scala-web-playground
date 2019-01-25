package io.jokester.scala_server_playground.util

import java.time.Instant
import java.util.{Random, UUID}

trait Entropy {
  def nextUUID(): UUID

  def currentServerTime(): Instant

  def currentTimestampInMS(): Long = {
    val now = currentServerTime()
    now.getEpochSecond * 1000 + now.getNano / 1000000
  }
}

class ControlledEntropy(var r: Random,
                        var currentServerTime: Instant =
                          RealWorld.currentServerTime())
    extends Entropy {
  override def nextUUID(): UUID = new UUID(r.nextLong(), r.nextLong())
}

object RealWorld extends Entropy {
  override def nextUUID(): UUID = UUID.randomUUID()

  override def currentServerTime(): Instant = Instant.now()
}
