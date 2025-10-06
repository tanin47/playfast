package framework

import givers.form.{BindContext, Mapping, UnbindContext, ValidationException, ValidationMessage}
import play.api.libs.json.{JsDefined, JsLookupResult, JsString, JsValue}

import scala.reflect.{ClassTag, classTag}
import scala.util.{Failure, Success, Try}

object Helpers {
  def makeValidationException(key: String): ValidationException = {
    val msg = new ValidationMessage(key)
    println(msg)
    new ValidationException(Seq(new ValidationMessage(key)))
  }

  def enumForm[T <: Enum[T]: ClassTag]: Mapping[T] = new Mapping[T] {
    def bind(value: JsLookupResult, context: BindContext): Try[T] = {
      value match {
        case JsDefined(v: JsString) =>
          val method = classTag[T].runtimeClass.getMethod("valueOf", classOf[String])
          try {
            Success(method.invoke(null, v.value).asInstanceOf[T])
          } catch {
            case _: Exception =>
              Failure(Mapping.error("error.invalid"))
          }
        case _ => Failure(Mapping.error("error.invalid"))
      }
    }

    def unbind(value: T, context: UnbindContext): JsValue = throw new UnsupportedOperationException()
  }

}
