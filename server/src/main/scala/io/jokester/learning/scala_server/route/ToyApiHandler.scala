//package io.jokester.learning.scala_server.route
//
//import akka.http.scaladsl.server.Route
//import akka.stream.Materializer
//import io.jokester.learning.scala_server.repo.{ ToyAction, ToyRepo }
//import io.jokester.learning.scala_server.util.Logging
//
//import scala.concurrent.{ ExecutionContext, Future }
//
//class ToyApiHandler(repo: ToyRepo)(implicit val executionContext: ExecutionContext) extends IToyApiHandler with Logging {
//
//  override def ReduceState(respond: ToyApiResource.ReduceStateResponse.type)(body: ToyToyAction): Future[ToyApiResource.ReduceStateResponse] = {
//
//    val action = body.delta.map(ToyAction)
//
//    for {
//      s <- action.map(repo.reduceS).getOrElse(repo.getS)
//    } yield {
//      logger.debug("ReduceState: {} -> {}", body: Any, s: Any)
//
//      ToyApiResource.ReduceStateResponseOK(
//        ToyToyState(changedCount = Some(s.revision), number = Some(s.value)))
//    }
//  }
//
//  override def ReadState(respond: ToyApiResource.ReadStateResponse.type)(): Future[ToyApiResource.ReadStateResponse] = {
//
//    for {
//      s <- repo.getS
//    } yield {
//      logger.debug("ReadState", s: Any)
//
//      ToyApiResource.ReadStateResponseOK(
//        ToyToyState(changedCount = Some(s.revision), number = Some(s.value)))
//    }
//  }
//
//  def route(implicit m: Materializer): Route = {
//    ToyApiResource.routes(this)
//  }
//}
