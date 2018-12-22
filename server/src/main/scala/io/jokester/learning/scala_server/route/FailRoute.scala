package io.jokester.learning.scala_server.route

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.BasicDirectives
import io.jokester.learning.scala_server.util.Logging

object FailRoute extends BasicDirectives with Logging {

  def route: Route = extractRequest { req =>
    logger.debug("unmatched request: {} {}", req.method: Any, req.uri: Any)
    complete(StatusCode.int2StatusCode(404), "unmatched request")
  }
}
