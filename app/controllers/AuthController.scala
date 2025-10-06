package controllers

import database.services.UserService.EmailAlreadyExistingException
import database.services.{EmailVerificationTokenService, ForgotPasswordTokenService, UserService}
import framework.*
import framework.Helpers.makeValidationException
import givers.form.Form
import givers.form.Mappings.{email, opt, text}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent}
import services.EmailService

import javax.inject.{Inject, Singleton}
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

  val REGISTER_FORM: Form[RegisterData] = Form(
    "validation.register",
    RegisterData.apply,
    Tuples.to[RegisterData],
    "email" -> email,
    "password" -> text(allowEmpty = false)
  )

  case class VerifyEmailData(
    userId: String,
    secretToken: String
  )

  val VERIFY_EMAIL_FORM: Form[VerifyEmailData] = Form(
    "validation.verifyEmail",
    VerifyEmailData.apply,
    Tuples.to[VerifyEmailData],
    "userId" -> text(allowEmpty = false),
    "secretToken" -> text(allowEmpty = false)
  )

  case class ForgotPasswordData(
    email: String,
    dummy: Option[String]
  )

  val FORGOT_PASSWORD_FORM: Form[ForgotPasswordData] = Form(
    "validation.forgotPassword",
    ForgotPasswordData.apply,
    Tuples.to[ForgotPasswordData],
    "email" -> email,
    "dummy" -> opt(text(allowEmpty = false))
  )

  case class ResetPasswordData(
    userId: String,
    secretToken: String,
    password: String
  )

  val RESET_PASSWORD_FORM: Form[ResetPasswordData] = Form(
    "validation.resetPassword",
    ResetPasswordData.apply,
    Tuples.to[ResetPasswordData],
    "userId" -> text(allowEmpty = false),
    "secretToken" -> text(allowEmpty = false),
    "password" -> text(allowEmpty = false)
  )
}

@Singleton
class AuthController @Inject() (
  userService: UserService,
  emailService: EmailService,
  forgotPasswordTokenService: ForgotPasswordTokenService,
  emailVerificationTokenService: EmailVerificationTokenService,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BaseController(cc) {
  import AuthController.*

  def login(): Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.auth.login()))
  }

  def doLogin(): Action[JsValue] = async(parse.json) { implicit req =>
    val data = LOGIN_FORM.bindFromRequest().get

    for {
      user <- userService
        .getByEmail(data.email)
        .map(_.getOrElse { throw makeValidationException("validation.login.email.error.notExist") })
    } yield {
      if (!user.hashedPassword.exists { pw => new BCryptPasswordEncoder().matches(data.password, pw) }) {
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
            password = Some(data.password)
          )
        )
        .recover { case EmailAlreadyExistingException =>
          throw makeValidationException("validation.register.email.error.alreadyExists")
        }
      _ <- emailService.sendVerifyEmail(user)
    } yield {
      setLoggedInUser(Ok(Json.obj()), user)
    }
  }

  def logout(): Action[AnyContent] = async() { implicit req =>
    Future(clearLoggedInUser(Redirect("/")))
  }

  def resendVerifyEmail(): Action[JsValue] = authenticated(parse.json) { implicit req =>
    for {
      _ <- emailService.sendVerifyEmail(req.loggedInUser)
    } yield {
      Ok(Json.obj())
    }
  }

  def verifyEmail(userId: String, secretToken: String): Action[AnyContent] = async() { implicit req =>
    for {
      user <- userService.getById(userId).map(_.get)
      isValid <- emailVerificationTokenService.isValid(userId = userId, token = secretToken)
    } yield {
      if (!isValid) {
        throw new NotFoundException()
      }

      Ok(views.html.auth.verifyEmail(userId, secretToken))
    }
  }

  def doVerifyEmail(): Action[AnyContent] = async() { implicit req =>
    val data = VERIFY_EMAIL_FORM.bindFromRequest().get

    for {
      user <- userService.getById(data.userId).map(_.get)
      isValid <- emailVerificationTokenService.isValid(userId = data.userId, token = data.secretToken)
      _ = if (!isValid) {
        throw new NotFoundException()
      }
      _ <- userService.verifyEmail(user.id)
      _ <- forgotPasswordTokenService.delete(user.id, data.secretToken)
      user <- userService.getById(user.id).map(_.get)
    } yield {
      setLoggedInUser(Ok(Json.obj()), user)
    }
  }

  def forgotPassword(): Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.auth.forgotPassword()))
  }

  def doForgotPassword(): Action[JsValue] = async(parse.json) { implicit req =>
    val data = FORGOT_PASSWORD_FORM.bindFromRequest().get

    for {
      user <- userService
        .getByEmail(data.email)
        .map(_.getOrElse {
          throw Helpers.makeValidationException("validation.forgotPassword.email.error.notExist")
        })
      token <- forgotPasswordTokenService.create(user.id)
      _ <- emailService.sendResetPasswordEmail(user, token.token)
    } yield {
      Ok(Json.obj())
    }
  }

  def resetPassword(userId: String, secretToken: String): Action[AnyContent] = async() { implicit req =>
    for {
      user <- userService.getById(userId).map(_.get)
      isValid <- forgotPasswordTokenService.isValid(user.id, secretToken)
      _ = if (!isValid) {
        throw new NotFoundException()
      }
    } yield {
      Ok(views.html.auth.resetPassword(userId, secretToken))
    }
  }

  def doResetPassword(): Action[AnyContent] = async() { implicit req =>
    val data = RESET_PASSWORD_FORM.bindFromRequest().get

    for {
      user <- userService.getById(data.userId).map(_.get)
      isValid <- forgotPasswordTokenService.isValid(user.id, data.secretToken)
      _ = if (!isValid) {
        throw new NotFoundException()
      }
      _ <- userService.updatePassword(user.id, Some(data.password))
      _ <- forgotPasswordTokenService.delete(user.id, data.secretToken)
      user <- userService.getById(user.id).map(_.get)
    } yield {
      setLoggedInUser(Ok(Json.obj()), user)
    }
  }
}
