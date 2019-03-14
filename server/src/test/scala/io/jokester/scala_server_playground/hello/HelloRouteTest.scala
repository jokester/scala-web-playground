package io.jokester.scala_server_playground.hello

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

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

  "patch route" should {
    "support patch method" in {

      Patch("/try-patch") ~> route ~> check {
        responseAs[String] shouldEqual "patch!"
      }

      Post("/try-patch") ~> route ~> check {
        response.status shouldEqual StatusCodes.NotFound
      }

      Post("/try-patch?_method=PATCH") ~> route ~> check {
        responseAs[String] shouldEqual "patch!"
      }

    }

  }

}
