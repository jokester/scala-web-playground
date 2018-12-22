package io.jokester.scala_server_playground.toy

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.generic.auto._
import io.circe.syntax._
import io.jokester.scala_server_playground.util.Catch404
import scalikejdbc.DB

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

class ToyHandler(getDB: () => DB)(implicit e: ExecutionContext) extends Catch404 {

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
    } ~
      post {
        path(IntNumber) { rev =>
          path(IntNumber) { delta =>
            complete(s"$rev /$delta")
          }
        }
      }
  }

  def route: Route = {
    pathPrefix("unsafe-mem")(subroute(unsafeMemRepo)) ~
      pathPrefix("db")(subroute(dbRepo)) ~
      catchUnmatched
  }
}
