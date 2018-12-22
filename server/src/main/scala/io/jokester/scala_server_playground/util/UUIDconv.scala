package io.jokester.scala_server_playground.util

import java.sql.PreparedStatement
import java.util.UUID

import scalikejdbc.{ ParameterBinderFactory, ParameterBinderWithValue }
import spray.json.{ JsString, JsValue, JsonFormat }

object UUID4pg {

  implicit val uuidBinder: ParameterBinderFactory[UUID] = (uuid: UUID) => new ParameterBinderWithValue {
    val value: UUID = uuid

    override def toString(): String = s"uuid(${uuid.toString})"

    override def apply(stmt: PreparedStatement, idx: Int): Unit = {
      stmt.setObject(idx, uuid)
    }
  }
}

object UUID4json {
  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x toString ())

    def read(value: JsValue): UUID = value match {
      case JsString(x) => UUID.fromString(x)
    }
  }
}