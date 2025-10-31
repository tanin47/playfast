package modules

import background.JobRunrMain
import com.google.inject.{AbstractModule, Provider}
import play.api.inject.ApplicationLifecycle
import play.api.{Application, Configuration, Logger, Mode}
import tanin.backdoor.BackdoorServer

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BackdoorServerProvider @Inject() (
  app: Application,
  config: Configuration,
  lifecycle: ApplicationLifecycle
)(implicit
  ec: ExecutionContext
) extends Provider[BackdoorServer] {
  private[this] val logger = Logger(this.getClass)

  lazy val backdoorServer: BackdoorServer = new BackdoorServer(
    config.get[String]("slick.dbs.default.db.properties.url"),
    9999
  )

  def get(): BackdoorServer = {
    if (!JobRunrMain.isRunning && app.mode != Mode.Test) {
      // Not background processing nor test. Starting Backdoor...
      lifecycle.addStopHook(() => Future { backdoorServer.stop() })
      backdoorServer.start()
    }
    backdoorServer
  }
}

class BackdoorModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[BackdoorServer])
      .toProvider(classOf[BackdoorServerProvider])
      .asEagerSingleton()
  }
}
