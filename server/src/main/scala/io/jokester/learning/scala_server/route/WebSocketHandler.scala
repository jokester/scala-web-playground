package io.jokester.learning.scala_server.route

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.ws.Message
import akka.stream._
import akka.stream.scaladsl._

class WebSocketHandler(system: ActorSystem) {

  private val echo = Flow[Message]

  def route: Route = path("ws" / "echo") {
    handleWebSocketMessages(echo)
  }
}