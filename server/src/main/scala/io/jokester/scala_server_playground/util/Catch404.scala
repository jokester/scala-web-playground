package io.jokester.scala_server_playground.util

import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Directives.{ complete, extractRequest }
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

trait Catch404 extends LazyLogging {
  def catchUnmatched: Route = extractRequest { req =>
    logger.debug("unmatched request: {} {}", req.method.value, req.uri)
    complete(StatusCodes.NotFound, HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"nnnonot found: ${req.method.value} ${req.uri}"))
  }
}
