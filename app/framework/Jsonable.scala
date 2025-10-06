package framework

import givers.form.FilledForm
import play.api.libs.json.*

trait Jsonable {
  def toJson(): JsObject
}

object Jsonable {
  def encode(items: Seq[Jsonable]): String = {
    encodeJson(items.map(_.toJson()))
  }

  def encode(valueOpt: Option[Jsonable]): String = {
    encodeJson(valueOpt.map(_.toJson()))
  }

  def encode(value: Jsonable): String = {
    encodeJson(value.toJson())
  }

  def encode(value: String): String = {
    encodeJson(JsString(value))
  }

  def encode(value: Boolean): String = {
    encodeJson(JsBoolean(value))
  }

  def encode(value: Int): String = {
    encodeJson(JsNumber(value))
  }

  def encode(value: Long): String = {
    encodeJson(JsNumber(value))
  }

  def encode(form: FilledForm[_]): String = {
    encodeJson(form.toJson)
  }

  def encodeJson(value: JsValue): String = {
    sanitize(value.toString)
  }

  def encodeJson(items: Seq[JsValue]): String = {
    sanitize(JsArray(items).toString)
  }

  def encodeJson(valueOpt: Option[JsValue]): String = {
    valueOpt.map(encodeJson).getOrElse(JsNull.toString)
  }

  protected[this] def sanitize(value: String): String = {
    value.replaceAll("<", "\\\\u003C")
  }
}
