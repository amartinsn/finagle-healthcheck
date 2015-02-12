package io.amartinsn.finagle.healthcheck

import com.twitter.util.Future

import scala.collection.concurrent.TrieMap

/**
 * Created by amartins on 2/11/15.
 */
class HealthCheckRegistry {

  private[this] val healthChecks = TrieMap[String, HealthCheck]()

  def register(name: String, healthCheck: HealthCheck) =
    healthChecks.putIfAbsent(name, healthCheck)

  def unregister(name: String) =
    healthChecks.remove(name)

  def names = collection.immutable.SortedSet(
    healthChecks.keySet.toList: _*)

  def runHealthCheck(name: String): Future[Result] = {
    val maybeHealthCheck = healthChecks.get(name)

    if (maybeHealthCheck.isDefined)
      new NoSuchElementException(s"No healthcheck found with name $name")

    maybeHealthCheck.get.execute()
  }

  def runHealthChecks(): Future[Map[String, Result]] = {
    Future.collect(
      (healthChecks map { e => (e._1, e._2.execute()) }).toMap
    )
  }
}
