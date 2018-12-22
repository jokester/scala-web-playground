package io.jokester.scala_server_playground.chatroom

import org.scalatest.{ Matchers, WordSpec }

class WsInterfaceTest extends WordSpec with Matchers with WsInterface {

  "decode" should {

    "decode ping message" in {
      val decoded = decode(""" { "tag": { "method": "Ping", "nonce": "n" } } """)
      decoded shouldBe a[Messages.Ping]
    }

    "reject incorrect message" in {
      val decoded = decode(""" { "tag": { "method": "pping", "nonce": "n" } } """)
      decoded shouldBe a[Messages.DecodeFail]
    }
  }
}
