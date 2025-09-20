package controllers

import database.services.UserService
import database.services.UserService.EmailAlreadyExistingException
import framework.Helpers.makeValidationException
import framework.{BaseController, ControllerComponents, Tuples}
import givers.form.Form
import givers.form.Mappings.{email, text}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import play.api.*
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

object AuthController {
  case class LoginData(
    email: String,
    password: String
  )

  val LOGIN_FORM: Form[LoginData] = Form(
    "validation.login",
    LoginData.apply,
    Tuples.to[LoginData],
    "email" -> email,
    "password" -> text(allowEmpty = false)
  )

  case class RegisterData(
    email: String,
    password: String
  )

  val REGISTER_FORM: Form[LoginData] = Form(
    "validation.register",
    LoginData.apply,
    Tuples.to[LoginData],
    "email" -> email,
    "password" -> text(allowEmpty = false)
  )
}

@Singleton
class AuthController @Inject() (userService: UserService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BaseController(cc) {
  import AuthController.*

  def login(): Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.auth.login()))
  }

  def doLogin(): Action[JsValue] = async(parse.json) { implicit req =>
    val data = LOGIN_FORM.bindFromRequest().get

    println(data.password)

    for {
      user <- userService
        .getByEmail(data.email)
        .map(_.getOrElse { throw makeValidationException("validation.login.email.error.notExist") })
    } yield {
      if (!new BCryptPasswordEncoder().matches(data.password, user.hashedPassword)) {
        throw makeValidationException("validation.login.password.error.invalid")
      }

      setLoggedInUser(Ok(Json.obj()), user)
    }
  }

  def register(): Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.auth.register()))
  }

  def doRegister(): Action[JsValue] = async(parse.json) { implicit req =>
    val data = LOGIN_FORM.bindFromRequest().get

    for {
      user <- userService
        .create(
          UserService.CreateData(
            email = data.email,
            password = data.password
          )
        )
        .recover { case EmailAlreadyExistingException =>
          throw makeValidationException("validation.register.email.error.alreadyExists")
        }
    } yield {
      setLoggedInUser(Ok(Json.obj()), user)
    }
  }

  def logout(): Action[AnyContent] = async() { implicit req =>
    Future(clearLoggedInUser(Redirect("/")))
  }
}
