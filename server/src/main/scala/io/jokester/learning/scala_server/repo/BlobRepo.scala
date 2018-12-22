package io.jokester.learning.scala_server.repo

import java.io.{ ByteArrayInputStream, InputStream }
import java.nio.ByteBuffer
import java.util.UUID

import com.google.common.io.ByteStreams
import io.jokester.learning.scala_server.util.{ Entropy, UUID4pg }
import scalikejdbc.{ DB, _ }

import scala.util.Try

case class Blob(id: UUID, contentType: String, bytes: ByteBuffer) {

  private lazy val copyiedBytes: ByteBuffer = {
    val b = bytes.asReadOnlyBuffer()
    b.rewind()
    val byteArray = new Array[Byte](b.limit())
    b.put(byteArray)
    ByteBuffer.wrap(byteArray).asReadOnlyBuffer()
  }

  lazy val bytesArray: Array[Byte] = {
    if (bytes.hasArray) bytes.array() else copyiedBytes.array()
  }

  def bytesStream(): InputStream = new ByteArrayInputStream(bytesArray)

  lazy val meta: BlobMeta = BlobMeta(id, contentType, bytesLength)

  lazy val bytesLength: Int = {
    bytes.asReadOnlyBuffer().rewind().limit()
  }
}

case class BlobMeta(id: UUID, contentType: String, numBytes: Int) {

}

object Blob extends SQLSyntaxSupport[Blob] {
  override def tableName: String = "blobs"

  def apply(g: ResultName[Blob])(rs: WrappedResultSet): Blob = {
    val b = ByteBuffer.wrap(rs.bytes(g.bytes))
    Blob(id = UUID.fromString(rs.string(g.id)), contentType = rs.string(g.contentType), bytes = b)
  }
}

trait BlobRepo {
  def saveBlob(contentType: String, bytes: ByteBuffer): BlobMeta

  def saveBlob(contentType: String, in: InputStream): BlobMeta

  def saveBlob(b: Blob): BlobMeta

  def getBlob(id: UUID): Option[Blob]

  def getBlob(id: String): Option[Blob] = Try {
    UUID.fromString(id)
  }.fold(e => None, getBlob)
}

class BlobRepoImpl(getDB: () => DB)(implicit e: Entropy) extends BlobRepo {

  import UUID4pg._

  private val b = Blob.syntax("b")

  override def saveBlob(contentType: String, bytes: ByteBuffer): BlobMeta = {
    saveBlob(Blob(e.nextUUID(), contentType, bytes))
  }

  override def saveBlob(contentType: String, in: InputStream): BlobMeta = {
    val bytes = ByteBuffer.wrap(ByteStreams.toByteArray(in))
    saveBlob(contentType, bytes)
  }

  override def saveBlob(b: Blob): BlobMeta = {
    getDB() localTx { implicit session =>
      sql""" INSERT INTO blobs (id, content_type, bytes) VALUES (uuid(${b.id}), ${b.contentType}, ${b.bytesStream(): InputStream}) """
        .update().apply()
    }
    b.meta
  }

  override def getBlob(id: UUID): Option[Blob] = {
    getDB() readOnly { implicit session =>
      withSQL {
        selectFrom(Blob as b)
          .where.eq(b.id, id)
      }
        .map(rs => Blob(b.resultName)(rs)).single().apply()
    }
  }
}