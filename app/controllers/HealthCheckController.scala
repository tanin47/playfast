package controllers

import framework.{BaseController, ControllerComponents, Instant}
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.AnyContent

import javax.inject.*
import scala.concurrent.ExecutionContext

@Singleton
class HealthCheckController @Inject() (
  dbConfigProvider: DatabaseConfigProvider,
  cc: ControllerComponents
)(implicit
  ec: ExecutionContext
) extends BaseController(cc) {

  import framework.PostgresProfile.api.*

  private val logger = Logger(getClass)

  def index(): play.api.mvc.Action[AnyContent] = async() { implicit req =>
    // We want the health check mechanism to hit the database.
    val startedAt = Instant.now().toEpochMilli
    dbConfigProvider.get.db.run { sql"SELECT 1".as[Int] }.map { resp =>
      val elapsed = Instant.now().toEpochMilli - startedAt

      if (elapsed > 5000) {
        logger.warn(s"Health-checking took too long (${elapsed}ms)")
      }

      Ok(s"Succeeded. Took ${elapsed}ms.")
    }
  }
}
