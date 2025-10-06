package database.services

import database.models.{EmailVerificationToken, EmailVerificationTokenTable}
import framework.{BaseDbService, Instant}
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class EmailVerificationTokenService @Inject() (
  val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends BaseDbService {

  import framework.PostgresProfile.api.*

  private val query: TableQuery[EmailVerificationTokenTable] = TableQuery[EmailVerificationTokenTable]

  def create(userId: String): Future[EmailVerificationToken] = {
    val now = Instant.now()
    val entity = EmailVerificationToken(
      userId = userId,
      token = Random.alphanumeric.take(32).mkString,
      createdAt = now
    )

    db
      .run {
        query += entity
      }
      .map { _ => entity }
  }

  def get(
    userId: String,
    token: String,
    since: Instant
  ): Future[Option[EmailVerificationToken]] = {
    db.run {
      query
        .filter { t => t.userId === userId && t.token === token && t.createdAt >= since }
        .result
        .headOption
    }
  }

  def isValid(userId: String, token: String): Future[Boolean] = {
    get(userId, token, Instant.now().minus(24, ChronoUnit.HOURS))
      .map(_.isDefined)
  }

  def delete(userId: String, token: String): Future[Int] = {
    db.run {
      query.filter { t => t.userId === userId && t.token === token }.delete
    }
  }

  def getByUserId(userId: String): Future[Option[EmailVerificationToken]] = {
    db.run {
      query
        .filter { t => t.userId === userId }
        .result
        .headOption
    }
  }
}
