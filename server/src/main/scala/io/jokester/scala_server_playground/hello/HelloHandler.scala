package io.jokester.scala_server_playground.hello

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directive0, Directives, Route}
import com.typesafe.scalalogging.LazyLogging
import io.jokester.scala_server_playground.util.Catch404

class HelloHandler extends LazyLogging with Catch404 with Directives {
  def route: Route = {
    (get & pathEndOrSingleSlash) {
      logger.debug("hello")
      complete(
        HttpEntity(ContentTypes.`text/plain(UTF-8)`, "hello from HelloHandler")
      )
    } ~ tryPatchAlternative ~ catchUnmatched
  }

  def patchRoute: Route = complete("patch!")

  def patchFromX: Directive0 = {
    post & overrideMethodWithParameter("_method") & patch
  }

  def tryPatchAlternative: Route = {
    path("try-patch") {
      (patch | patchFromX) {
        patchRoute
      }
    }
  }

}
