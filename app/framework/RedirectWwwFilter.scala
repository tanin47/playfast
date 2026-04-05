package framework

import org.apache.pekko.stream.Materializer
import play.api.mvc.{Filter, RequestHeader, Result}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RedirectWwwFilter @Inject() (config: PlayConfig)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  def apply(
    nextFilter: RequestHeader => Future[Result]
  )(req: RequestHeader): Future[Result] = {
    if (req.host != config.BASE_URL.replaceAll("https?://", "") && req.method == "GET") {
      Future(play.api.mvc.Results.Redirect(s"${config.BASE_URL}${req.uri}"))
    } else {
      nextFilter.apply(req)
    }
  }
}
