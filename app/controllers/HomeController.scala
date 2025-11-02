package controllers

import background.TestIncrementDummyCounterWorkerRequest
import database.services.UserService
import framework.{BaseController, ControllerComponents}
import org.jobrunr.scheduling.JobRequestScheduler
import play.api.mvc.AnyContent

import java.time.Instant
import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject() (
  userService: UserService,
  cc: ControllerComponents,
  jobScheduler: JobRequestScheduler
)(implicit ec: ExecutionContext)
    extends BaseController(cc) {

  def index(): play.api.mvc.Action[AnyContent] = async() { implicit req =>
    println("In controller: " + Instant.now())
    req.loggedInUserOpt.foreach { loggedInUser =>
      jobScheduler.enqueue(TestIncrementDummyCounterWorkerRequest(loggedInUser.id))
    }
    Future(Ok(views.html.index()))
  }

  def privacy(): play.api.mvc.Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.static.privacy()))
  }

  def tos(): play.api.mvc.Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.static.tos()))
  }
}
