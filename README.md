# finagle-healthcheck

First create a healthcheck registry, where you are going to register all your healthchecks...
```scala
  val registry = new HealthCheckRegistry()
```

Then create some healthchecks for crucial components used by your system.
```scala
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
```  

Register them onto your previously defined registry.
```scala
  registry.register("Service", serviceCheck)
  registry.register("Memcached", memcachedCheck)
  registry.register("MySql", mySqlCheck)
```

Create a new RunHealthChecks Finagle service passing your registry, and start a server to serve this service on a given port.
```scala
  Await.ready(
    Httpx.serve(":8083", RunHealthChecks(registry))
  )
```

Then just call **http://localhost:8083** and you will get this:
```
{
  Memcached: {
    healthy: true
  },
  Service: {
    healthy: true
  },
  MySql: {
    healthy: false,
    message: "Lost connection to server...",
    error: {
      messge: "Lost connection to server...",
      stack:  [
        "stack trace goes here..."
        .
        .
        .
      ]
    }
  }
}
```
