package io.jokester.scala_server_playground.toy

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCode }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

object ToyRoute extends LazyLogging {

  val helloRoute: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "hello!"))
      }
    }

  def toyRoute: Route = path("toy") {
    helloRoute ~ route404
  }

  private def route404: Route = extractRequest { req =>
    logger.debug("unmatched request: {} {}", req.method: Any, req.uri: Any)
    complete(StatusCode.int2StatusCode(404), "unmatched request")
  }
}
