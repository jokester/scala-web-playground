package io.jokester.learning.scala_server

import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

class HelloRouteTest extends WordSpec with Matchers with ScalatestRouteTest {

  val route: Route = HelloRoute.helloRoute

  "The service" should {

    "return a greeting for GET requests to the root path" in {
      // tests:
      Get("/hello") ~> route ~> check {
        responseAs[String] shouldEqual "not found"
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
