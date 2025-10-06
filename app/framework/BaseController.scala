package framework

import database.models.User
import database.services.UserService
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.typedmap.TypedMap
import play.api.mvc.*
import play.api.mvc.request.{RemoteConnection, RequestTarget}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

class Request[A](
  val loggedInUserOpt: Option[User],
  val req: play.api.mvc.Request[A],
  val config: PlayConfig
) extends play.api.mvc.Request[A] {

  override def body: A = req.body

  override def connection: RemoteConnection = req.connection

  override def method: String = req.method

  override def target: RequestTarget = req.target

  override def version: String = req.version

  override def headers: Headers = req.headers

  override def attrs: TypedMap = req.attrs
}

class AuthRequest[A](override val req: Request[A], override val config: PlayConfig)
    extends Request[A](req.loggedInUserOpt, req.req, config) {
  val loggedInUser: User = loggedInUserOpt.get
}

@Singleton
case class ControllerComponents @Inject() (
  userService: UserService,
  messagesActionBuilder: MessagesActionBuilder,
  actionBuilder: DefaultActionBuilder,
  parsers: PlayBodyParsers,
  messagesApi: MessagesApi,
  langs: Langs,
  fileMimeTypes: FileMimeTypes,
  config: PlayConfig,
  executionContext: scala.concurrent.ExecutionContext
) extends play.api.mvc.MessagesControllerComponents

object BaseController {
  val USER_ID_SESSION_KEY = "user"
}

abstract class BaseController(cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends play.api.mvc.MessagesAbstractController(cc) {
  import BaseController.*

  def async[T](parser: BodyParser[T] = parse.anyContent)(fn: Request[T] => Future[Result]): Action[T] =
    Action.async(parser) { baseReq =>
      for {
        req <- convert(baseReq)
        result <- fn(req)
      } yield {
        result
      }
    }

  def authenticated[T](parser: BodyParser[T] = parse.anyContent)(fn: AuthRequest[T] => Future[Result]): Action[T] = {
    async(parser) { req =>

      if (req.loggedInUserOpt.isEmpty) {
        throw new AuthenticationRequiredException
      }

      fn(new AuthRequest[T](req, req.config))
    }
  }

  def setLoggedInUser(result: Result, loggedInUser: User)(implicit req: RequestHeader): Result = {
    result.addingToSession(USER_ID_SESSION_KEY -> loggedInUser.id)
  }

  def clearLoggedInUser(result: Result)(implicit req: RequestHeader): Result = {
    result.removingFromSession(USER_ID_SESSION_KEY)
  }

  private[this] def convert[T](baseReq: play.api.mvc.Request[T]): Future[Request[T]] = {
    val userIdOpt =
      try {
        baseReq.session.get(USER_ID_SESSION_KEY)
      } catch {
        case _: Exception => None
      }

    for {
      userOpt <- userIdOpt match {
        case Some(userId) => cc.userService.getById(userId)
        case None         => Future.successful(None)
      }
    } yield {
      framework.Request(userOpt, baseReq, cc.config)
    }
  }
}
