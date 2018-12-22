package io.jokester.learning.scala_server.toy

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.jokester.scala_server_playground.hello.HelloHandler
import org.scalatest.{ Matchers, WordSpec }

class HelloRouteTest extends WordSpec with Matchers with ScalatestRouteTest {

  val route: Route = (new HelloHandler).route

  "The service" should {

    "return a greeting for GET requests to the root path" in {
      // tests:
      Get("/") ~> route ~> check {
        responseAs[String] shouldEqual "hello from HelloHandler"
      }
    }

  }

  "anon route" should {
    val r = get {
      complete {
        "404 noooot found"
      }
    }

    "returns not found" in {
      Get("/a/b/c") ~> r ~> check {
        responseAs[String] shouldEqual "404 noooot found"
      }
    }
  }
}
