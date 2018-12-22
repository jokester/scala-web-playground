package io.jokester.scala_server_playground.hello

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import com.typesafe.scalalogging.LazyLogging

class HelloHandler extends LazyLogging {
  def route: Route = {
    (get & pathEnd) {
      logger.debug("hello")
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "hello from HelloHandler"))
    } ~ extractRequest { req =>
      logger.debug("unmatched request: {} {}", req.method.value, req.uri)
      complete(StatusCodes.NotFound, HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"nnnonot found: ${req.method.value} ${req.uri}"))
    }
  }
}
