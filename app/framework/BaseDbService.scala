package framework

import org.postgresql.util.PSQLException
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

abstract class BaseDbService(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  def matchUniqueConstraintException(
    e: PSQLException,
    constraint: String
  ): Boolean = {
    e.getSQLState == "23505" && (
      // Postgres automatically truncate an index name to 63 characters. Read more:  https://pgpedia.info/n/NAMEDATALEN.html
      e.getServerErrorMessage.getConstraint == constraint.take(63)
        || e.getServerErrorMessage.getConstraint == constraint
    )
  }
}
