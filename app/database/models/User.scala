package database.models

import database.models.User.PreferredLang
import framework.PostgresProfile.api.*
import framework.{Instant, Jsonable}
import play.api.libs.json.{JsObject, Json}
import slick.lifted.{ProvenShape, Rep}

object User {
  enum PreferredLang extends Enum[PreferredLang] {
    case English, Thai, Japanese, German
  }
}

case class User(
  id: String,
  email: String,
  hashedPassword: Option[String],
  isEmailVerified: Boolean,
  preferredLang: Option[User.PreferredLang],
  shouldReceiveNewsletter: Boolean,
  dummyCounter: Int,
  createdAt: Instant
) extends Jsonable {
  def toJson(): JsObject = Json.obj(
    "id" -> id,
    "email" -> email,
    "isEmailVerified" -> isEmailVerified,
    "preferredLang" -> preferredLang.map(_.toString),
    "shouldReceiveNewsletter" -> shouldReceiveNewsletter,
    "dummyCounter" -> dummyCounter,
    "createdAt" -> createdAt.toEpochMilli
  )

}

class UserTable(tag: Tag) extends Table[User](tag, "user") {
  def id: Rep[String] = column[String]("id", O.PrimaryKey, O.AutoInc)
  def email: Rep[String] = column[String]("email")
  def hashedPassword: Rep[Option[String]] = column[Option[String]]("hashed_password")
  def isEmailVerified: Rep[Boolean] = column[Boolean]("is_email_verified")
  def preferredLang: Rep[Option[PreferredLang]] = column[Option[User.PreferredLang]]("preferred_lang")
  def shouldReceiveNewsletter: Rep[Boolean] = column[Boolean]("should_receive_newsletter")
  def dummyCounter: Rep[Int] = column[Int]("dummy_counter")
  def createdAt: Rep[Instant] = column[Instant]("created_at")

  def * : ProvenShape[User] = (
    id,
    email,
    hashedPassword,
    isEmailVerified,
    preferredLang,
    shouldReceiveNewsletter,
    dummyCounter,
    createdAt
  ).<>((User.apply _).tupled, User.unapply)
}
