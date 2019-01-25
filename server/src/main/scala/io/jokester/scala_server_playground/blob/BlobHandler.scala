package io.jokester.scala_server_playground.blob

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.StreamConverters
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class BlobHandler(repo: BlobRepo) extends LazyLogging with BlobSerialization {

  private def uploadRoute(implicit m: Materializer): Route =
    (path("multipart") & post & pathEnd & withSizeLimit(16777216)) {
      fileUpload("blob") {
        case (metadata, bytesSource) =>
          val stream = bytesSource.runWith(
            StreamConverters.asInputStream(FiniteDuration(10, TimeUnit.SECONDS))
          )
          val meta = repo.saveBlob(metadata.getContentType.toString(), stream)
          complete(meta)
      }
    }

  private def getRoute(implicit m: Materializer): Route =
    (path(Segment) & get & pathEnd) { uuidStr =>
      complete(Future.fromTry(Try {
        val b = repo.getBlob(uuidStr)
        toRes(b)
      }))
    }

  def route(implicit m: Materializer): Route = uploadRoute ~ getRoute
}
