package database.models

import framework.Jsonable
import framework.PostgresProfile.api.*
import play.api.libs.json.{JsObject, Json}
import slick.lifted.{ProvenShape, Rep}

import java.time.Instant

case class ForgotPasswordToken(
  userId: String,
  token: String,
  createdAt: Instant
) extends Jsonable {
  def toJson(): JsObject = Json.obj(
    "userId" -> userId,
    "token" -> token,
    "createdAt" -> createdAt.toEpochMilli
  )
}

class ForgotPasswordTokenTable(tag: Tag) extends Table[ForgotPasswordToken](tag, "forgot_password_token") {
  def userId: Rep[String] = column[String]("user_id")
  def token: Rep[String] = column[String]("token")
  def createdAt: Rep[Instant] = column[Instant]("created_at")

  def * : ProvenShape[ForgotPasswordToken] = (
    userId,
    token,
    createdAt
  ).<>((ForgotPasswordToken.apply _).tupled, ForgotPasswordToken.unapply)
}
