package io.jokester.scala_server_playground.hello
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }

class HelloHandler {
  def route: Route = {
    get {
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "hello from HelloHandler"))
    }
  }
}
