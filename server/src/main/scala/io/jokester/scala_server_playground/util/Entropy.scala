package io.jokester.scala_server_playground.util

import java.util.{Random, UUID}

trait Entropy {
  def nextUUID(): UUID
}

class ControlledEntropy(val r: Random) extends Entropy {
  override def nextUUID(): UUID = new UUID(r.nextLong(), r.nextLong())
}

object RealWorld extends Entropy {
  override def nextUUID(): UUID = UUID.randomUUID()
}
