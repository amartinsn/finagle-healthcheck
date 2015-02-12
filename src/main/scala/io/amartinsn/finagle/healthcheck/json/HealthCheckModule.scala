package io.amartinsn.finagle.healthcheck.json

import com.fasterxml.jackson.core.{Version, JsonGenerator}
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind.module.SimpleSerializers
import com.fasterxml.jackson.databind.{JsonSerializer, Module, SerializerProvider}
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import io.amartinsn.finagle.healthcheck.Result

/**
 * Created by amartins on 2/12/15.
 */
object HealthCheckModule extends HealthCheckModule

class HealthCheckModule extends Module {
  import scala.collection.JavaConverters._

  def getModuleName: String = "healthchecks"

  def version(): Version = Version.unknownVersion()

  def setupModule(context: SetupContext) = {
    val list = List[JsonSerializer[_]](
      new HealthCheckResultSerializer
    ).asJava

    context.addSerializers(new SimpleSerializers(list))
  }
}

private[this] class HealthCheckResultSerializer extends StdSerializer[Result](classOf[Result]) {
  def serialize(result: Result, json: JsonGenerator, provider: SerializerProvider) = {
    json.writeStartObject()
    json.writeBooleanField("healthy", result.healthy)

    val message = result.message
    if (message.isDefined)
      json.writeStringField("message", message.get)

    val error = result.error
    if (error.isDefined)
      serializeThrowable(json, error.get, "error")

    json.writeEndObject()
  }

  private[this] def serializeThrowable(json: JsonGenerator, error: Throwable, name: String): Unit = {
    json.writeObjectFieldStart(name)
    json.writeStringField("messge", error.getMessage)
    json.writeArrayFieldStart("stack")

    for(element <- error.getStackTrace) {
      json.writeString(element.toString)
    }

    json.writeEndArray()
    if (error.getCause != null) {
      serializeThrowable(json, error.getCause, "cause")
    }

    json.writeEndObject()
  }
}
