package background

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object Helpers {

  private[background] def await[T](future: Future[T]): T = Await.result(future, Duration.Inf)
}
