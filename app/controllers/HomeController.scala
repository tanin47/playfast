package controllers

import database.services.UserService
import framework.{BaseController, ControllerComponents}
import play.api.*
import play.api.mvc.AnyContent

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HomeController @Inject() (userService: UserService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BaseController(cc) {

  def index(): play.api.mvc.Action[AnyContent] = async() { implicit req =>
    Future(Ok(views.html.index()))
  }

  def privacy() = async() { implicit req =>
    Future(Ok(views.html.static.privacy()))
  }

  def tos() = async() { implicit req =>
    Future(Ok(views.html.static.tos()))
  }
}
