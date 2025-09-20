package framework

import com.google.inject.Inject
import framework.BaseController.USER_ID_SESSION_KEY
import org.apache.pekko.stream.Materializer
import play.api.Logger
import play.api.mvc.{Filter, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

class LoggingFilter @Inject() (
  errorHandler: ErrorHandler
)(implicit
  val mat: Materializer,
  ec: ExecutionContext
) extends Filter {
  private[this] val logger = Logger(getClass)

  def apply(
    nextFilter: RequestHeader => Future[Result]
  )(requestHeader: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis
    val shouldSkip = requestHeader.uri.startsWith("/assets")

    if (!shouldSkip) {
      val userIdOpt = requestHeader.session.get(USER_ID_SESSION_KEY)
      logger.info(
        s"${requestHeader.method} ${requestHeader.uri} (id:${requestHeader.id}) started (user:${userIdOpt.getOrElse("")})"
      )
    }

    nextFilter(requestHeader)
      .recoverWith { case e: Throwable =>
        errorHandler.onServerError(requestHeader, e)
      }
      .map { result =>
        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime

        if (shouldSkip && (result.header.status >= 200 && result.header.status < 400)) {
          // Nothing is failing.
          // Skip logging
        } else {
          val userIdOpt = result.session(requestHeader).get(USER_ID_SESSION_KEY)
          val log =
            s"${requestHeader.method} ${requestHeader.uri} (id:${requestHeader.id}) took ${requestTime}ms (size: ${result.body.contentLength.getOrElse("unknown")}) and returned ${result.header.status} (user:${userIdOpt.getOrElse("")})"

          logger.info(log)
        }

        result
      }
  }
}
