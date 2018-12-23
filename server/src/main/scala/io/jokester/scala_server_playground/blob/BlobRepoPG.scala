package io.jokester.scala_server_playground.blob

import java.io.InputStream
import java.nio.ByteBuffer
import java.util.UUID

import com.google.common.io.ByteStreams
import io.jokester.scala_server_playground.util.Entropy
import scalikejdbc.{DB, ResultName, WrappedResultSet, _}

class BlobRepoPG(getDB: () => DB)(implicit e: Entropy) extends BlobRepo {

  object BlobTable extends SQLSyntaxSupport[Blob] {
    override def tableName: String = "blobs"

    def apply(g: ResultName[Blob])(rs: WrappedResultSet): Blob = {
      val b = ByteBuffer.wrap(rs.bytes(g.bytes))
      Blob(id = UUID.fromString(rs.string(g.id)), contentType = rs.string(g.contentType), bytes = b)
    }
  }

  private val b = BlobTable.syntax("b")

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
      sql"SELECT * FROM blobs WHERE id = $id"
        .map(readBlob).single.apply
    }
  }

  def readBlob(rs: WrappedResultSet): Blob = {
    // NOTE: pg uuid type can be read with rs.string()
    Blob(
      id = UUID.fromString(rs.string("id")),
      contentType = rs.string("content_type"),
      bytes = ByteBuffer.wrap(ByteStreams.toByteArray(rs.binaryStream("bytes"))))
  }

  def readBlobMeta(rs: WrappedResultSet): BlobMeta = {
    BlobMeta(
      id = UUID.fromString(rs.string("id")),
      contentType = rs.string("content_type"),
      numBytes = rs.int("len_bytes"))
  }

  def getBlobMeta(id: UUID): Option[BlobMeta] = {
    getDB() readOnly { implicit session =>
      sql"SELECT id, content_type, LEN(bytes) FROM blobs WHERE id = $id"
        .map(readBlobMeta).single.apply
    }
  }
}
