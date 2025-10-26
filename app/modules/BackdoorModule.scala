package modules

import com.google.inject.{AbstractModule, Provider}
import play.api.inject.ApplicationLifecycle
import play.api.{Configuration, Logger}
import tanin.backdoor.BackdoorServer

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BackdoorServerProvider @Inject() (
  config: Configuration,
  lifecycle: ApplicationLifecycle
)(implicit
  ec: ExecutionContext
) extends Provider[BackdoorServer] {
  private[this] val logger = Logger(this.getClass)

  lazy val backdoorServer: BackdoorServer = {
    val server = new BackdoorServer(
      config.get[String]("slick.dbs.default.db.properties.url"),
      9999
    )

    lifecycle.addStopHook(() => Future { server.stop() })

    server.start()
    server
  }

  def get(): BackdoorServer = backdoorServer
}

class BackdoorModule extends AbstractModule {
  override def configure() = {
    bind(classOf[BackdoorServer])
      .toProvider(classOf[BackdoorServerProvider])
      .asEagerSingleton()
  }
}
