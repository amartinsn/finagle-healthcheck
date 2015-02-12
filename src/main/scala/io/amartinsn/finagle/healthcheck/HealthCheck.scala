package io.amartinsn.finagle.healthcheck

import com.twitter.util.Future

/**
 * Created by amartins on 2/11/15.
 */
object Result {
  val healthy = Result(healthy=true)

  def healthy(message: String) =
    Result(healthy=true, Some(message))

  def unhealthy(message: String) =
    Result(healthy=false, Some(message))

  def unhealthy(error: Throwable) =
    Result(healthy=false, Some(error.getMessage), Some(error))
}

case class Result(
  healthy: Boolean,
  message: Option[String] = None,
  error: Option[Throwable] = None)

trait HealthCheck {
  def check(): Future[Result]

  def execute() = {
    try {
      check()
    } catch {
      case e: Exception =>
        Future(Result.unhealthy(e))
    }
  }
}
