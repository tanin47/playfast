package database.services

import database.models.{ForgotPasswordToken, ForgotPasswordTokenTable}
import framework.{BaseDbService, Instant}
import play.api.db.slick.DatabaseConfigProvider
import slick.lifted.TableQuery

import java.time.temporal.ChronoUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class ForgotPasswordTokenService @Inject() (
  val dbConfigProvider: DatabaseConfigProvider
)(implicit ec: ExecutionContext)
    extends BaseDbService {
  import framework.PostgresProfile.api.*

  private val query: TableQuery[ForgotPasswordTokenTable] = TableQuery[ForgotPasswordTokenTable]

  def create(userId: String): Future[ForgotPasswordToken] = {
    val now = Instant.now()
    val entity = ForgotPasswordToken(
      userId = userId,
      token = Random.alphanumeric.take(32).mkString,
      createdAt = now
    )

    db
      .run { query += entity }
      .map { _ => entity }
  }

  def get(
    userId: String,
    token: String,
    since: Instant
  ): Future[Option[ForgotPasswordToken]] = {
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

  def getByUserId(userId: String): Future[Option[ForgotPasswordToken]] = {
    db.run {
      query
        .filter { t => t.userId === userId }
        .result
        .headOption
    }
  }
}
