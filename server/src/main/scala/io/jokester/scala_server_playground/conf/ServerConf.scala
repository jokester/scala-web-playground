package io.jokester.scala_server_playground.conf

import java.net.URI

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{ConnectionPool, DB}
import scalikejdbc.config.DBs

import scala.util.Try

object ServerConf extends LazyLogging {
  private lazy val conf = ConfigFactory.load()

  lazy val redisConf: Option[RedisConf] =
    Try {
      val r = new URI(conf.getConfig("redis.default").getString("uri"))
      RedisConf(host = r.getHost, port = r.getPort, auth = None)
    }.toOption

  DBs.setupAll()
  private val cPool = ConnectionPool()

  def getDB() = DB(cPool.borrow())
}

case class RedisConf(host: String, port: Int, auth: Option[String] = None)
