package io.jokester.scala_server_playground.hello

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import com.typesafe.scalalogging.LazyLogging
import io.jokester.scala_server_playground.util.Catch404

class HelloHandler extends LazyLogging with Catch404 {
  def route: Route = {
    (get & pathEndOrSingleSlash) {
      logger.debug("hello")
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "hello from HelloHandler"))
    } ~ catchUnmatched
  }
}
