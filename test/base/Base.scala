package base

import database.models.User
import database.services.UserService
import framework.Instant
import org.openqa.selenium.StaleElementReferenceException
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.db.evolutions.EvolutionsApi
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration, Mode}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import java.util.concurrent.TimeUnit
import scala.concurrent.Future

object Base {
  val PORT = 9002
  val OPEN_AI_REQUEST_CACHE_TABLE = "open_ai_request_test_cache"
  val IS_MAC: Boolean = sys.props("os.name").toLowerCase.contains("mac")

  lazy val appConfig: Map[String, Any] = Map(
    "slick.dbs.default.db.properties.url" -> "postgres://play_fast_dev_user:dev@localhost:5432/play_fast_test",
    "play.evolutions.enabled" -> false
  )

  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Configuration.from(appConfig))
    .in(Mode.Test)
    .build()

  case class WhatsappParticipant(
    phoneNumber: String,
    nameOpt: Option[String] = None
  )

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      import play.api.test.Helpers
      Helpers.await(app.stop(), 30, TimeUnit.SECONDS)
    }
  })
}

class Base extends AnyFunSpec with BeforeAndAfter with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  lazy val app = Base.app

  lazy val dbConfigProvider: DatabaseConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  lazy val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  lazy val userService: UserService = app.injector.instanceOf[UserService]

  def await[T](future: Future[T]): T = {
    import play.api.test.Helpers
    Helpers.await(future, 120, TimeUnit.SECONDS)
  }

  def resetDatabase(): Unit = {
    import framework.PostgresProfile.api._

    val db = dbConfig.db

    val tables = await(db.run {
      sql"SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename ASC;"
        .as[String]
    })

    tables
      .filterNot(_ == Base.OPEN_AI_REQUEST_CACHE_TABLE)
      .foreach { table =>
        await(db.run {
          sqlu"""DROP TABLE IF EXISTS "#$table" CASCADE;"""
        })
      }

    app.injector.instanceOf[EvolutionsApi].applyFor("default")
  }

  override def beforeEach(): Unit = {
    resetDatabase()
    super.beforeEach()
    Instant.mockTimeForTest(java.time.Instant.parse("2025-09-22T07:00:00Z"))
  }

  private[this] val WAIT_UNTIL_TIMEOUT_MILLIS = 15000
  def waitUntil(fn: => Boolean): Unit = {
    val newFn = () => {
      try {
        fn
      } catch {
        case _: StaleElementReferenceException   => false
        case _: NoSuchElementException           => false
        case _: java.util.NoSuchElementException => false
      }
    }

    val startTime = System.currentTimeMillis()
    while ((System.currentTimeMillis() - startTime) < WAIT_UNTIL_TIMEOUT_MILLIS) {
      Thread.sleep(250)

      if (newFn()) return
    }

    throw new TestFailedException("waitUntil failed.", 1)
  }

  def makeUser(
    email: String = "",
    password: String = ""
  ): User = {
    await(
      userService.create(
        UserService.CreateData(
          email = email,
          password = password
        )
      )
    )
  }
}
