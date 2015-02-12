package io.amartinsn.finagle.healthcheck

import com.twitter.util.{Await, Future}
import org.junit.runner.RunWith
import org.scalatest.{Matchers, FunSpec}
import org.scalatest.junit.JUnitRunner

/**
 * Created by amartins on 2/11/15.
 */
@RunWith(classOf[JUnitRunner])
class HealthCheckSpec extends FunSpec with Matchers {

  it ("executes its check implementation returning result") {
    val result = Await.result(new HealthCheck() {
      def check() = Future.value(Result.healthy("I'm healthy!"))
    }.execute())

    result.healthy should be(true)
    result.error   should be(None)
    result.message should be(Some("I'm healthy!"))
  }

  it ("rescues any exception on its check returning an unhealthy result") {
    val result = Await.result(new HealthCheck() {
      def check() = throw new RuntimeException("boo!")
    }.execute())

    result.healthy should be(false)
    result.error.get.getClass should
      be(classOf[RuntimeException])
    result.message should be(Some("boo!"))
  }
}
