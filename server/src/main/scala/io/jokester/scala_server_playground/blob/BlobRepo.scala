package io.jokester.scala_server_playground.blob

import java.io.InputStream
import java.nio.ByteBuffer
import java.util.UUID

import scala.util.Try

trait BlobRepo {
  def saveBlob(contentType: String, bytes: ByteBuffer): BlobMeta

  def saveBlob(contentType: String, in: InputStream): BlobMeta

  def saveBlob(b: Blob): BlobMeta

  def getBlob(uuid: UUID): Option[Blob]

  final def getBlob(uuid: String): Option[Blob] = {
    toUUID(uuid).flatMap(getBlob)
  }

  def getBlobMeta(id: UUID): Option[BlobMeta]

  private def toUUID(uuid: String): Option[UUID] =
    Try {
      UUID.fromString(uuid)
    }.toOption
}
