package io.jokester.scala_server_playground.blob

import java.nio.ByteBuffer

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class BlobRepoTest extends WordSpec with Matchers with BeforeAndAfterAll {

  "BlobRepo" should {

    import io.jokester.scala_server_playground.TestUtil.{entropy, getDB}

    val repo = new BlobRepoPG(getDB)

    "save and load blobs" in {
      val bytes = ByteBuffer.allocate(32)
      val blobSrc = Blob(entropy.nextUUID(), "image/jpeg", bytes)

      repo.saveBlob(blobSrc)

      val blobGot = repo.getBlob(blobSrc.id)

      blobGot.isDefined shouldBe true
      blobGot.get shouldEqual blobSrc
    }

    "allocate uuid for un-ided blob" in {
      val bytes = ByteBuffer.allocate(16)

      val meta = repo.saveBlob("image/png", bytes)

      val blobGot = repo.getBlob(meta.id)

      blobGot.isDefined shouldBe true
      blobGot.get shouldEqual Blob(meta.id, meta.contentType, bytes)
    }
  }
}
