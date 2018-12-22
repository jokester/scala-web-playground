package io.jokester.scala_server_playground.ws

import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl._

class WsEchoHandler() {
  def route: Route = {
    path("echo") {
      val echo = Flow[Message]
      handleWebSocketMessages(echo)
    }
  }
}