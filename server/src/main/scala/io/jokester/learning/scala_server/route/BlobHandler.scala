package io.jokester.learning.scala_server.route

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import akka.util.ByteString
import io.jokester.learning.scala_server.repo.{ Blob, BlobMeta, BlobRepo }
import io.jokester.learning.scala_server.util.Logging
import spray.json.{ DefaultJsonProtocol, RootJsonFormat }

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

trait BlobSerialization extends SprayJsonSupport with DefaultJsonProtocol with Logging {
  import io.jokester.learning.scala_server.util.UUID4json.UuidJsonFormat

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
        logger.warn(s"failed to parse content type: {} / {}", s: Any, errors: Any)
        ContentTypes.`application/octet-stream`
      case Right(m) => m
    }
  }
}

class BlobHandler(repo: BlobRepo) extends Logging with BlobSerialization {

  private def uploadRoute(implicit m: Materializer): Route = path("blob") {
    withSizeLimit(16777216) {
      fileUpload("img") {
        case (metadata, bytesSource) =>
          val stream = bytesSource.runWith(StreamConverters.asInputStream(FiniteDuration(10, TimeUnit.SECONDS)))
          val meta = repo.saveBlob(metadata.getContentType.toString(), stream)
          complete(meta)
      }
    }
  }

  private def getRoute(implicit m: Materializer): Route = path("blob" / Segment) { uuidStr =>
    get {
      complete(Future.fromTry(Try {
        val b = repo.getBlob(uuidStr)
        toRes(b)
      }))
    }
  }

  def route(implicit m: Materializer): Route = uploadRoute ~ getRoute

}
