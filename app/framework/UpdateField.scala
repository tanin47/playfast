package framework

import givers.form.{BindContext, Mapping, UnbindContext}
import play.api.libs.json.{JsLookupResult, JsString, JsValue}

import scala.reflect.{ClassTag, classTag}
import scala.util.{Success, Try}

sealed trait UpdateField[T] {
  def toOption: Option[T] = this match {
    case UpdateField.Update(u) => (Some(u): Option[T])
    case UpdateField.NoUpdate  => None
  }
}

object UpdateField {
  val UNSET_KEYWORD = "SET_TO_NULL"
  case class Update[T](value: T) extends UpdateField[T]
  case object NoUpdate extends UpdateField[Nothing]

  def form[T: ClassTag](mapping: Mapping[T]): Mapping[UpdateField[T]] = new Mapping[UpdateField[T]] {
    def bind(value: JsLookupResult, context: BindContext): Try[UpdateField[T]] = {
      value.toOption match {
        case Some(v: JsString) if v.value == UNSET_KEYWORD && classTag[T].runtimeClass.getSimpleName == "Option" =>
          Success(UpdateField.Update(None).asInstanceOf[UpdateField[T]])
        case Some(v) => mapping.bind(value, context).map { v => UpdateField.Update(v) }
        case None    => Success(UpdateField.NoUpdate.asInstanceOf[UpdateField[T]])
      }
    }

    def unbind(value: UpdateField[T], context: UnbindContext): JsValue = {
      throw new UnsupportedOperationException("UpdateField cannot be unbounded into a JsValue.")
    }
  }
}
