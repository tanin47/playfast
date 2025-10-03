package database.services

import database.models.{User, UserTable}
import framework.UpdateField.NoUpdate
import framework.{BaseDbService, Instant, UpdateField}
import org.postgresql.util.PSQLException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

object UserService {
  case class CreateData(
    email: String,
    password: Option[String],
    createdAt: Instant = Instant.now()
  )

  case class UpdateData(
    preferredLang: UpdateField[Option[User.PreferredLang]] = NoUpdate,
    shouldReceiveNewsletter: UpdateField[Boolean] = NoUpdate,
    isEmailVerified: UpdateField[Boolean] = NoUpdate,
    hashedPassword: UpdateField[Option[String]] = NoUpdate
  )

  def sanitizeEmail(email: String): String = email.toLowerCase.trim

  case object EmailAlreadyExistingException extends Exception

  def hashPassword(password: String): String = {
    new BCryptPasswordEncoder().encode(password)
  }
}

@Singleton
class UserService @Inject() (
  val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends BaseDbService {
  import UserService.*
  import framework.PostgresProfile.api.*

  val query: TableQuery[UserTable] = TableQuery[UserTable]

  def create(data: CreateData): Future[User] = {
    val entity = User(
      id = "",
      email = data.email,
      hashedPassword = data.password.map(hashPassword),
      isEmailVerified = false,
      preferredLang = None,
      shouldReceiveNewsletter = false,
      createdAt = data.createdAt
    )

    val future = for {
      id <- db.run {
        (query returning query.map(_.id)) += entity
      }
    } yield {
      entity.copy(id = id)
    }

    future.recoverWith {
      case e: PSQLException if matchUniqueConstraintException(e, "user__email") =>
        throw EmailAlreadyExistingException
    }
  }

  def update(id: String, data: UpdateData): Future[Unit] = {
    val base = query.filter(_.id === id)

    val updates = Seq(
      data.preferredLang.toOption.map { v => base.map(_.preferredLang).update(v) },
      data.shouldReceiveNewsletter.toOption.map { v => base.map(_.shouldReceiveNewsletter).update(v) },
      data.isEmailVerified.toOption.map { v => base.map(_.isEmailVerified).update(v) },
      data.hashedPassword.toOption.map { v => base.map(_.hashedPassword).update(v) }
    ).flatten

    db.run(DBIO.sequence(updates).transactionally).map(_ => ())

  }

  def getByEmail(email: String): Future[Option[User]] = {
    db.run {
      query.filter(_.email === email).result.headOption
    }
  }

  def getById(id: String): Future[Option[User]] = {
    db.run {
      query.filter(_.id === id).result.headOption
    }
  }

  def updatePassword(id: String, password: Option[String]): Future[Unit] = {
    update(
      id = id,
      data = UpdateData(
        hashedPassword = UpdateField(password.map(hashPassword)),
        isEmailVerified = UpdateField(true)
      )
    )
  }

  def verifyEmail(id: String): Future[Unit] = {
    update(
      id = id,
      data = UpdateData(
        isEmailVerified = UpdateField(true)
      )
    )
  }
}
