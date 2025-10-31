package modules

import com.google.inject.AbstractModule
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.jobrunr.configuration.{JobRunr, JobRunrConfiguration}
import org.jobrunr.scheduling.JobRequestScheduler
import org.jobrunr.server.{BackgroundJobServerConfiguration, JobActivator}
import org.jobrunr.storage.StorageProvider
import org.jobrunr.storage.sql.common.SqlStorageProviderFactory
import play.api.inject.ApplicationLifecycle
import play.api.{Application, Configuration, Logger, Mode}

import java.time.Duration
import javax.inject.{Inject, Provider, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StorageProviderProvider @Inject() (
  config: Configuration,
  lifecycle: ApplicationLifecycle
)(implicit
  ec: ExecutionContext
) extends Provider[StorageProvider] {
  lazy val provider: StorageProvider = {
    val dbConfig = new HikariConfig()
    dbConfig.setDataSource({
      val d = new slick.jdbc.DatabaseUrlDataSource()
      d.setDriverClassName(config.get[String]("slick.dbs.default.db.properties.driver"))
      d.setUrl(config.get[String]("slick.dbs.default.db.properties.url"))
      d.setDeregisterDriver(true)
      d
    })
    // Render and others put a limit on the Postgres connections.
    // Please be mindful because:
    // During a zero-downtime deployment, there'll be 2 instances running. Therefore, the number of connections used will be doubled.
    // In the background where the parallelization is low, we don't need that many connections.
    dbConfig.setMaximumPoolSize(3)
    dbConfig.setMinimumIdle(3)
    dbConfig.setConnectionTimeout(10000)
    dbConfig.setValidationTimeout(5000)

    val dataSource = new HikariDataSource(dbConfig)
    lifecycle.addStopHook(() => Future(dataSource.close()))

    val provider = SqlStorageProviderFactory.using(dataSource)
    lifecycle.addStopHook(() => Future { provider.close() })

    provider
  }

  def get(): StorageProvider = provider
}

@Singleton
class JobRunrBaseConfiguration @Inject() (
  app: Application,
  storageProvider: StorageProvider
)(implicit
  ec: ExecutionContext
) extends Provider[JobRunrConfiguration] {
  private[this] val logger = Logger(this.getClass)

  // It is important that the scheduler and the background runner uses the same queue config.
  // It's also important that this is a def, so it works in test.
  def get(): JobRunrConfiguration = JobRunr
    .configure()
    .useStorageProvider(storageProvider)
    .useJobActivator(new JobActivator {
      def activateJob[T](tpe: Class[T]): T = app.injector.instanceOf[T](tpe)
    })
    .useBackgroundJobServer(
      BackgroundJobServerConfiguration
        .usingStandardBackgroundJobServerConfiguration()
        .andWorkerCount(3)
        .andInterruptJobsAwaitDurationOnStopBackgroundJobServer(
          app.mode match {
            case Mode.Dev | Mode.Test => Duration.ofSeconds(1)
            case Mode.Prod            => Duration.ofSeconds(200)
          }
        ),
      // Notice that the background server isn't set to start automatically.
      // This is because the config is used in the web service,
      // which only queues jobs (not runs them)
      false
    )
}

@Singleton
class JobRequestSchedulerProvider @Inject() (
  lifecycle: ApplicationLifecycle,
  baseConfiguration: JobRunrBaseConfiguration
)(implicit
  ec: ExecutionContext
) extends Provider[JobRequestScheduler] {
  private[this] val logger = Logger(this.getClass)

  lazy val scheduler: JobRequestScheduler = {
    val scheduler = baseConfiguration
      .get()
      .initialize()
      .getJobRequestScheduler()

    lifecycle.addStopHook(() => Future { scheduler.shutdown() })

    scheduler
  }

  def get(): JobRequestScheduler = scheduler
}

class JobRunrModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[StorageProvider])
      .toProvider(classOf[StorageProviderProvider])
    bind(classOf[JobRequestScheduler])
      .toProvider(classOf[JobRequestSchedulerProvider])
      .asEagerSingleton()
  }
}
