package services

import database.models.User
import database.services.EmailVerificationTokenService
import framework.{ExternalServiceException, PlayConfig}
import play.api.Logger
import play.api.libs.ws.*
import play.api.libs.ws.WSAuthScheme.BASIC

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService @Inject() (
  emailVerificationTokenService: EmailVerificationTokenService,
  ws: WSClient,
  config: PlayConfig
)(implicit ec: ExecutionContext) {
  private val logger = Logger(getClass)

  def sendVerifyEmail(user: User): Future[Unit] = {
    for {
      token <- emailVerificationTokenService.create(user.id)
    } yield {
      send(
        email = user.email,
        subject = "Verify your account",
        textBody = views.txt.email.verifyEmail(user, token, config).toString,
        htmlBody = views.html.email.verifyEmail(user, token, config).toString
      )
    }
  }

  def sendResetPasswordEmail(user: User, token: String): Future[Unit] = {
    send(
      email = user.email,
      subject = "Reset your password",
      textBody = views.txt.email.resetPassword(user, config, token).toString,
      htmlBody = views.html.email.resetPassword(user, config, token).toString
    )
  }

  def send(
    email: String,
    subject: String,
    textBody: String,
    htmlBody: String
  ): Future[Unit] = {
    ws
      .url(s"https://api.mailgun.net/v3/${config.MAILGUN_DOMAIN}/messages")
      .withAuth("api", config.MAILGUN_API_KEY, BASIC)
      .post(
        Map(
          "from" -> "support@play.nanakorn.com",
          "to" -> email,
          "subject" -> subject,
          "text" -> textBody,
          "html" -> htmlBody
        )
      )
      .map { resp =>
        if (resp.status >= 200 && resp.status < 300) {
          ()
        } else {
          throw new ExternalServiceException(resp.status, resp.body)
        }
      }
  }
}
