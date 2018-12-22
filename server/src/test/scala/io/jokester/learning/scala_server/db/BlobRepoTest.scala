package io.jokester.learning.scala_server.db

import java.nio.ByteBuffer
import java.util.Random

import io.jokester.learning.scala_server.repo.{ Blob, BlobRepoImpl }
import io.jokester.learning.scala_server.util.{ ControlledEntropy, Entropy }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import scalikejdbc.config.DBs
import scalikejdbc.{ ConnectionPool, DB }

class BlobRepoTest extends WordSpec with Matchers with BeforeAndAfterAll {

  "BlobRepo" should {
    DBs.setupAll()
    val cPool = ConnectionPool('test)
    implicit val entropy: Entropy = new ControlledEntropy(new Random)
    val repo = new BlobRepoImpl(() => DB(cPool.borrow()))

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
