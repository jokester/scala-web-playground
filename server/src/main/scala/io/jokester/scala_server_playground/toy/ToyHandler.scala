package io.jokester.scala_server_playground.toy

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import scalikejdbc.DB

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

class ToyHandler(getDB: () => DB)(implicit e: ExecutionContext) {

  private implicit def convert(toyState: ToyState): HttpResponse = {
    val body = HttpEntity(ContentTypes.`application/json`, toyState.asJson.noSpaces)
    HttpResponse(entity = body)
  }

  implicit def extractAction() = {

  }

  val unsafeMemRepo = new UnsafeMemoryToyRepo
  val syncMemRepo = new SynchronizedMemoryToyRepo
  val dbRepo = new ToyDatabaseRepo(getDB)

  def subroute(repo: ToyRepo): Route = {
    (pathEnd & get) {
      onComplete(repo.getState) {
        case Success(state) => complete(state: HttpResponse)
        case Failure(ex) => failWith(ex)
      }
    }
  }

  def route: Route = {
    path("unsafe-mem")(subroute(unsafeMemRepo)) ~
      path("db")(subroute(dbRepo))
  }
}
