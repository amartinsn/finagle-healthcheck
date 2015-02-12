package io.amartinsn.finagle.healthcheck

import com.twitter.util.{Await, Future}
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfter, Matchers, FunSpec}
import org.scalatest.junit.JUnitRunner

import scala.collection.immutable.SortedSet

/**
 * Created by amartins on 2/11/15.
 */
@RunWith(classOf[JUnitRunner])
class HealthCheckRegistrySpec extends FunSpec with Matchers with BeforeAndAfter {

  var registry: HealthCheckRegistry = null
  val healthyCheck = new HealthCheck {
    def check() = Future(Result.healthy("I am successful!"))
  }
  val unhealthyCheck = new HealthCheck {
    def check() = Future(Result.unhealthy(
      new RuntimeException("Error!")))
  }

  before {
    registry = new HealthCheckRegistry()
  }

  it ("registers new healthchecks to it") {
    registry.names should be(empty)

    registry.register("foo", healthyCheck)
    registry.names should be(SortedSet[String]("foo"))

    registry.register("bar", healthyCheck)
    registry.names should be(SortedSet[String]("bar", "foo"))
  }

  it ("unregisters healthchecks from it") {
    Seq("foo", "bar", "zap") foreach { name =>
      registry.register(name, healthyCheck) }

    registry.names should be(SortedSet[String]("bar", "foo", "zap"))

    registry.unregister("foo")
    registry.names should be(SortedSet[String]("bar", "zap"))

    registry.unregister("zap")
    registry.names should be(SortedSet[String]("bar"))
  }

  it ("runs a single healthCheck by its name") {
    registry.register("foo", healthyCheck)

    val result = Await.result(registry.runHealthCheck("foo"))
    result.healthy should be(true)
    result.error should be(None)
    result.message should be(Some("I am successful!"))
  }

  it ("throws NoSuchElementException when trying to run an unexisting healthcheck") {
    an [NoSuchElementException] should be thrownBy
      Await.result(registry.runHealthCheck("unexisting"))
  }

  it ("runs multiple healthChecks and collect results for each one") {
    Seq("foo", "zap") map { n => registry.register(n, healthyCheck) }
    registry.register("bar", unhealthyCheck)

    val results = Await.result(registry.runHealthChecks())

    Seq("foo", "zap") map { name =>
      val result = results.get(name).get
      result.error should be(None)
      result.healthy should be(true)
      result.message should be(Some("I am successful!"))
    }

    val result = results.get("bar").get
    result.healthy should be(false)
    val throwable = result.error.get
    throwable.getClass should be(classOf[RuntimeException])
    result.message should be(Some("Error!"))
  }
}