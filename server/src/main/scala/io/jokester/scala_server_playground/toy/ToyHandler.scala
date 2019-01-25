package io.jokester.scala_server_playground.toy

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.circe.generic.auto._
import io.circe.syntax._
import io.jokester.scala_server_playground.util.Catch404
import scalikejdbc.DB

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ToyHandler(getDB: () => DB)(implicit e: ExecutionContext)
    extends Catch404 {

  private implicit def convert(toyState: ToyState): HttpResponse = {
    val body =
      HttpEntity(ContentTypes.`application/json`, toyState.asJson.noSpaces)
    HttpResponse(entity = body)
  }

  val unsafeMemRepo = new ToyRepoMemoryUnsafe
  val syncMemRepo = new ToyRepoMemorySynced
  val dbRepoBasic = new ToyDBRepoBasic(getDB)
  val dbRepoNolock = new ToyDBRepoNolock(getDB)

  def subroute(repo: ToyRepo): Route = {
    (get & pathEnd) {
      completeWith(repo.getState)
    } ~
      (post & path(IntNumber)) { delta =>
        completeWith(repo.mutateState(ToyAction(delta)))
      }
  }

  def completeWith(s: Future[ToyState]): Route = {
    onComplete(s) {
      case Success(state) => complete(state: HttpResponse)
      case Failure(ex)    => failWith(ex)
    }
  }

  def route: Route = {
    pathPrefix("mem-unsafe")(subroute(unsafeMemRepo)) ~
      pathPrefix("mem-synced")(subroute(syncMemRepo)) ~
      pathPrefix("db-basic")(subroute(dbRepoBasic)) ~
      pathPrefix("db-nolock")(subroute(dbRepoNolock)) ~
      catchUnmatched
  }
}
