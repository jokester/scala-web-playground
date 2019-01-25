package io.jokester.scala_server_playground.blob

import java.io.{ByteArrayInputStream, InputStream}
import java.nio.ByteBuffer
import java.util.UUID

final case class Blob(id: UUID, contentType: String, bytes: ByteBuffer) {

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

final case class BlobMeta(id: UUID, contentType: String, numBytes: Int) {}
