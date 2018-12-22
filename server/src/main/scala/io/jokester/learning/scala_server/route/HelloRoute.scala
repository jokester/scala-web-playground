package io.jokester.learning.scala_server.route

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

object HelloRoute {

  val helloRoute: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "not found"))
      }
    }
}
