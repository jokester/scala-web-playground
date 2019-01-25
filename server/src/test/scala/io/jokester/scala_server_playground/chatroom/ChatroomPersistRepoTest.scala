package io.jokester.scala_server_playground.chatroom

import java.time.Instant

import org.scalatest.{
  BeforeAndAfterEach,
  DiagrammedAssertions,
  Matchers,
  WordSpec
}

class ChatroomPersistRepoTest
    extends WordSpec
    with Matchers
    with BeforeAndAfterEach
    with DiagrammedAssertions {

  import io.jokester.scala_server_playground.TestUtil.{entropy, getDB}

  val repo = new ChatroomPersistRepo(getDB)

  val remoteFuture: Instant = Instant.ofEpochMilli(4102444800000L)

  val List(dummyUser1, dummyUser2) = List("user1", "user2")

  val List(dummyChannel1) = List("channel1")
//
  override protected def beforeEach(): Unit = {
    repo.cleanObject(remoteFuture)
  }

  "ChatroomPersistRepo" should {

    "create a user" in {
      val u = repo.createUser(dummyUser1)
      u.name shouldBe dummyUser1

      val uBack = repo.findUser(u.uuid)
      assert(uBack.contains(u))
    }

    "create a channel" in {
      val c = repo.createChannel(dummyChannel1)

      c.name shouldBe dummyChannel1
    }

    "create a message" in {
      val u = repo.createUser(dummyUser1)
      val c = repo.createChannel(dummyChannel1)

      val m = repo.createMessage(u, c, "MSG")

      val mBack = repo.listMessage(c.name)
      assert(mBack == List(m))
    }

    "returns created message in desc order" in {
      val u = repo.createUser(dummyUser1)
      val c = repo.createChannel(dummyChannel1)

      val created = (1 to 1000).map(i => {

        repo.createMessage(u, c, s"MSG$i")
      })
      val read = repo.listMessage(c.name, 500)

      assert(created.drop(500) == read)
    }
  }
}
