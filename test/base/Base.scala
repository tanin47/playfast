package base

import background.JobRunrMain
import base.Base.appConfig
import ch.qos.logback.classic.Level
import database.models.User
import database.services.{EmailVerificationTokenService, ForgotPasswordTokenService, UserService}
import framework.{Instant, PlayConfig}
import mockws.MockWSHelpers.Action
import org.jobrunr.jobs.Job
import org.jobrunr.jobs.states.StateName
import org.jobrunr.storage.StorageProviderUtils.DatabaseOptions
import org.jobrunr.storage.{Paging, StorageProvider}
import org.openqa.selenium.StaleElementReferenceException
import org.scalatest.exceptions.TestFailedException
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, BeforeAndAfterEach}
import org.slf4j.LoggerFactory
import play.api.db.evolutions.EvolutionsApi
import play.api.db.slick.DatabaseConfigProvider
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import play.api.test.Helpers.POST
import play.api.{Application, Configuration, Mode, inject}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import java.util.concurrent.TimeUnit
import scala.concurrent.Future
import scala.jdk.CollectionConverters.ListHasAsScala

object Base {
  val PORT = 9002
  val IS_MAC: Boolean = sys.props("os.name").toLowerCase.contains("mac")

  lazy val appConfig: Map[String, Any] = Map(
    "slick.dbs.default.db.properties.url" -> "postgres://play_fast_dev_user:dev@localhost:5432/play_fast_test",
    "play.evolutions.enabled" -> false,
    "app.baseUrl" -> s"http://localhost:$PORT"
  )
}

class Base extends AnyFunSpec with BeforeAndAfter with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Configuration.from(appConfig))
    .in(Mode.Test)
    .overrides(inject.bind[WSClient].to[FakeOrRealWSClient])
    .build()

  lazy val dbConfigProvider: DatabaseConfigProvider = app.injector.instanceOf[DatabaseConfigProvider]
  lazy val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  lazy val config: PlayConfig = app.injector.instanceOf[PlayConfig]
  lazy val ws: FakeOrRealWSClient = app.injector.instanceOf[FakeOrRealWSClient]

  lazy val userService: UserService = app.injector.instanceOf[UserService]
  lazy val forgotPasswordTokenService: ForgotPasswordTokenService = app.injector.instanceOf[ForgotPasswordTokenService]
  lazy val emailVerificationTokenService: EmailVerificationTokenService =
    app.injector.instanceOf[EmailVerificationTokenService]

  var idRunner: Int = 0

  def genId(): Int = {
    idRunner += 1
    idRunner
  }

  def await[T](future: Future[T]): T = {
    import play.api.test.Helpers
    Helpers.await(future, 120, TimeUnit.SECONDS)
  }

  def resetDatabase(): Unit = {
    import framework.PostgresProfile.api.*
    // Initialize play.api.db.evolutions.DefaultEvolutionsApi, so we can set the log level dynamically.
    app.injector.instanceOf[EvolutionsApi]

    // Silence the logs from evolutions
    LoggerFactory
      .getLogger("play.api.db.evolutions")
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .setLevel(Level.INFO)

    val db = dbConfig.db

    val tables = await(db.run {
      sql"SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename ASC;"
        .as[String]
    })

    tables.foreach { table =>
      await(db.run {
        sqlu"""DROP TABLE IF EXISTS "#$table" CASCADE;"""
      })
    }

    app.injector.instanceOf[EvolutionsApi].applyFor("default")

    LoggerFactory
      .getLogger("org.jobrunr")
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .setLevel(Level.WARN)
    app.injector.instanceOf[StorageProvider].setUpStorageProvider(DatabaseOptions.CREATE)
  }

  override def beforeEach(): Unit = {
    ws.clearMockedRoutes()
    ws.addMockedRoutes {
      // Mock Mailgun's endpoint
      case (POST, s"https://api.mailgun.net/v3/${config.MAILGUN_DOMAIN}/messages") =>
        Action { req => Ok(Json.obj()) }
    }
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
    email: String = s"test${genId()}@random.email",
    password: Option[String] = Some("pass")
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

  private[this] def getPendingBackgroundJobs(): Seq[Job] = {
    val storageProvider = app.injector.instanceOf[StorageProvider]
    Seq(StateName.ENQUEUED, StateName.PROCESSING).flatMap { state =>
      storageProvider
        .getJobList(state, Paging.AmountBasedList.ascOnCreatedAt(100000))
        .asScala
        .toList
    }
  }

  def runAllPendingBackgroundJobs(): Unit = {
    val storageProvider = app.injector.instanceOf[StorageProvider]
    getPendingBackgroundJobs()
      .foreach { job =>
        job.setAmountOfRetries(0)
        storageProvider.save(job)
      }

    val jobRunrMain = new JobRunrMain(app)
    jobRunrMain.backgroundJobServer.start()
    waitUntil { getPendingBackgroundJobs().isEmpty }
    jobRunrMain.backgroundJobServer.stop()
  }
}
