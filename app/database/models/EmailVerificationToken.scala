package database.models

import framework.PostgresProfile.api.*
import framework.{Instant, Jsonable}
import play.api.libs.json.{JsObject, Json}
import slick.lifted.{ProvenShape, Rep}

case class EmailVerificationToken(
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

class EmailVerificationTokenTable(tag: Tag) extends Table[EmailVerificationToken](tag, "email_verification_token") {
  def userId: Rep[String] = column[String]("user_id")
  def token: Rep[String] = column[String]("token")
  def createdAt: Rep[Instant] = column[Instant]("created_at")

  def * : ProvenShape[EmailVerificationToken] = (
    userId,
    token,
    createdAt
  ).<>((EmailVerificationToken.apply _).tupled, EmailVerificationToken.unapply)
}
