package io.jokester.scala_server_playground

import io.jokester.scala_server_playground.util.{Entropy, RealWorld}
import scalikejdbc.config.DBs
import scalikejdbc.{ConnectionPool, DB}

object TestUtil {
  DBs.setupAll()
  val cPool = ConnectionPool()

  implicit val entropy: Entropy = RealWorld

  def getDB() = {
    DB(cPool.borrow())
  }
}
