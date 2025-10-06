package base

import mockws.MockWS
import org.apache.pekko.stream.Materializer
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.{WSClient, WSRequest}

import javax.inject.{Inject, Singleton}

@Singleton
class FakeOrRealWSClient @Inject (realWSClient: StandaloneAhcWSClient)(implicit mat: Materializer) extends WSClient {

  private var routes: MockWS.Routes = { case _ =>
    throw new NotImplementedError()
  }

  def addMockedRoutes(moreRoutes: MockWS.Routes): Unit = {
    routes = moreRoutes.orElse(routes)
  }

  def clearMockedRoutes(): Unit = {
    routes = { case _ => throw new NotImplementedError() }
  }

  def underlying[T]: T = ???

  def url(url: String): WSRequest =
    FakeWSRequestHolder(
      routes = routes,
      realWSClient = realWSClient,
      url = url
    )

  def close(): Unit = {
    realWSClient.close()
  }
}
