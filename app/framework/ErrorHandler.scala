package framework

import com.google.inject.{Inject, Provider, Singleton}
import givers.form.ValidationException
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.routing.Router
import play.api.{Configuration, Environment, Logger, OptionalSourceMapper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticationRequiredException extends Exception

class UnauthorizedException extends Exception

class NotFoundException extends Exception

class RedirectException(val path: String) extends Exception

@Singleton
class ErrorHandler @Inject() (
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router],
  messagesApi: MessagesApi
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  val logger: Logger = Logger(this.getClass)

  override def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future(Results.NotFound(views.html.static.notFound()))
  }

  override def onServerError(
    request: RequestHeader,
    exception: Throwable
  ): Future[Result] = {
    val isAjax = request.headers.get("From-Fetch").contains("true")

    def logMundaneException(e: Exception): Unit = {
      logger.info(s"${request.method} ${request.uri} (${request.id}): Exception: $e")
    }

    exception match {
      case e: AuthenticationRequiredException => logMundaneException(e)
      case e: ValidationException             => logMundaneException(e)
      case e: RedirectException               => logMundaneException(e)
      case e: UnauthorizedException           => logMundaneException(e)
      case e: NotFoundException               => logMundaneException(e)
      case e                                  =>
        logger.warn(s"${request.method} ${request.uri} (${request.id}): An unknown error was thrown.", e)
    }

    exception match {
      case _: NoSuchElementException => Future(Results.NotFound(views.html.static.notFound()))
      case _: NotFoundException      => Future(Results.NotFound(views.html.static.notFound()))
      case r: RedirectException      => Future(Results.Redirect(r.path))
      case _: UnauthorizedException  =>
        if (isAjax) {
          Future(
            Results.Unauthorized(
              Json.obj(
                "errors" -> Seq("You are not allowed to access this page.")
              )
            )
          )
        } else {
          Future(Results.Unauthorized(views.html.static.unauthorized()))
        }
      case _: AuthenticationRequiredException =>
        Future(
          Results.BadRequest(
            Json.obj(
              "errors" -> Seq(
                messagesApi.apply("validation.requireLogin")(Lang("en"))
              )
            )
          )
        )
      case e: ValidationException if isAjax =>
        Future(
          Results.BadRequest(
            Json.obj(
              "errors" -> e.messages.map { message =>
                val translated =
                  messagesApi.apply(message.key, message.args: _*)(Lang("en"))

                if (translated == message.key) {
                  logger.error(
                    s"Translation error: ${message.key}. User sees: An unknown error has occurred. Please contact the administrator.",
                    e
                  )
                  "An unknown error has occurred. Please contact the administrator."
                } else {
                  translated
                }
              }.distinct
            )
          )
        )
      case other =>
        super.onServerError(request, other)
    }
  }
}
