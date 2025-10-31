package background

import background.Helpers.await
import database.services.UserService
import framework.Instant
import org.jobrunr.jobs.lambdas.{JobRequest, JobRequestHandler}
import play.api.Logger

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

case class TestIncrementDummyCounterWorkerRequest(userId: String) extends JobRequest {
  def getJobRequestHandler(): Class[TestIncrementDummyCounterWorker] = classOf[TestIncrementDummyCounterWorker]
}

@Singleton
class TestIncrementDummyCounterWorker @Inject() (
  userService: UserService
)(implicit ec: ExecutionContext)
    extends JobRequestHandler[TestIncrementDummyCounterWorkerRequest] {
  private[this] val logger = Logger(getClass)

  def run(req: TestIncrementDummyCounterWorkerRequest): Unit = {
    logger.info(s"TestIncrementDummyCounterWorker is doing work... (${Instant.now()})")
    await(userService.incrementDummyCounter(req.userId))
    logger.info(s"TestIncrementDummyCounterWorker finished... (${Instant.now()})")
  }
}
