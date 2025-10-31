package background

import modules.JobRunrBaseConfiguration
import org.jobrunr.configuration.JobRunrConfiguration
import org.jobrunr.dashboard.JobRunrDashboardWebServerConfiguration
import org.jobrunr.server.BackgroundJobServer
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Environment, Logger, Mode}

object JobRunrMain {
  private[this] val logger = Logger(getClass)
  var isRunning: Boolean = false

  def main(args: Array[String]): Unit = {
    isRunning = true
    val mode = args.headOption match {
      case Some("dev") => Mode.Dev
      case _           => Mode.Prod
    }
    logger.info(s"Starting background processing... (mode=$mode)")

    val app = GuiceApplicationBuilder(Environment.simple(mode = mode)).build()

    new JobRunrMain(app).initialize()

    Thread.currentThread().join()
  }
}

class JobRunrMain(app: Application) {
  lazy val jobRunrConfig: JobRunrConfiguration = app.injector
    .instanceOf[JobRunrBaseConfiguration]
    .get()
    .useDashboard(
      JobRunrDashboardWebServerConfiguration
        .usingStandardDashboardConfiguration()
        .andPort(8000)
    )

  // This will be used in tests
  lazy val backgroundJobServer: BackgroundJobServer = {
    val field = jobRunrConfig.getClass.getDeclaredField("backgroundJobServer")
    field.setAccessible(true)
    field.get(jobRunrConfig).asInstanceOf[BackgroundJobServer]
  }

  def initialize(): Unit = {
    val _ = jobRunrConfig.initialize()

    // Start the background server
    backgroundJobServer.start()
  }
}
