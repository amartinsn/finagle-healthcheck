package io.amartinsn.finagle.healthcheck

import com.twitter.finagle.Httpx
import com.twitter.util.{Await, Future}
import sun.net.ConnectionResetException

/**
 * Created by amartins on 2/12/15.
 */
object Demo extends com.twitter.app.App {

  val registry = new HealthCheckRegistry()

  val serviceCheck = new HealthCheck() {
    def check(): Future[Result] =
      Future.value(Result.healthy)
  }

  val memcachedCheck = new HealthCheck() {
    def check(): Future[Result] =
      Future.value(Result.healthy)
  }

  val mySqlCheck = new HealthCheck() {
    def check(): Future[Result] =
      Future.value(Result.unhealthy(
        new ConnectionResetException("Lost connection to server...")))
  }

  registry.register("Service", serviceCheck)
  registry.register("Memcached", memcachedCheck)
  registry.register("MySql", mySqlCheck)

  Await.ready(
    Httpx.serve(":8083", RunHealthChecks(registry))
  )
}
