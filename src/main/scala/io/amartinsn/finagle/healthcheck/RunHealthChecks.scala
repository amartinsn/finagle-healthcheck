package io.amartinsn.finagle.healthcheck

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finagle.Service
import com.twitter.finagle.httpx.Status.{InternalServerError, NotImplemented, Ok}
import com.twitter.finagle.httpx.{Request, Response}
import com.twitter.util.Future
import io.amartinsn.finagle.healthcheck.json.HealthCheckModule

/**
 * Created by amartins on 2/11/15.
 */
case class RunHealthChecks(registry: HealthCheckRegistry)
  extends Service[Request, Response] {

  val mapper = objectMapper

  def apply(req: Request): Future[Response] = {
    registry.runHealthChecks() map { results =>
      val response = Response()
      response.setContentTypeJson()
      response.headerMap.add("Cache-Control",
        "must-revalidate,no-cache,no-store")

      if (results.isEmpty)
        response.setStatusCode(NotImplemented.code)
      else if (allHealthy(results))
        response.setStatusCode(Ok.code)
      else
        response.setStatusCode(InternalServerError.code)

      response.contentString = mapper.
        writeValueAsString(results)

      response
    }
  }

  private[this] def allHealthy(
      results: Map[String, Result]) =
    results.filterNot { entry => entry._2.healthy }.isEmpty

  private[this] def objectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
      .registerModule(HealthCheckModule)
  }
}
