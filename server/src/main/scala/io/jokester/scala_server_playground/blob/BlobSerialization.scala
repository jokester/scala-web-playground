package io.jokester.scala_server_playground.blob

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait BlobSerialization
    extends SprayJsonSupport
    with DefaultJsonProtocol
    with LazyLogging {

  import io.jokester.scala_server_playground.util.UUID4json.UuidJsonFormat

  implicit val metaFormat: RootJsonFormat[BlobMeta] = jsonFormat3(BlobMeta)

  def toRes(blob: Blob): HttpResponse = {
    val body = ByteString(blob.bytesArray)
    val entity = HttpEntity.Strict(toContentType(blob.contentType), body)
    HttpResponse(entity = entity)
  }

  def toRes(maybeBlob: Option[Blob]): HttpResponse = {
    maybeBlob.map(toRes).getOrElse {
      HttpResponse(StatusCodes.NotFound, entity = "not found")
    }
  }

  def toContentType(s: String): ContentType = {
    ContentType.parse(s) match {
      case Left(errors) =>
        logger.warn(
          s"failed to parse content type: {} / {}",
          s: Any,
          errors: Any
        )
        ContentTypes.`application/octet-stream`
      case Right(m) => m
    }
  }
}
