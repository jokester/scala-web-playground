package io.jokester.scala_server_playground.toy

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import scalikejdbc.DB

import scala.concurrent.ExecutionContext

class ToyHandler(getDB: () => DB)(implicit e: ExecutionContext) {

  implicit def convert(toyState: ToyState) = {
    ???
  }

  implicit def extractAction() = {

  }

  val unsafeMemRepo = new UnsafeMemoryToyRepo
  val syncMemRepo = new SynchronizedMemoryToyRepo
  val dbRepo = new ToyDatabaseRepo(getDB)

  def subroute(repo: ToyRepo): Route = {
    ???
  }

  def route(): Route = {
    path("mem") {
      ???
    }
  }
}
