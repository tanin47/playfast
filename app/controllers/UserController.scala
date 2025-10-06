package controllers

import database.models.User
import database.services.UserService
import framework.*
import givers.form.Form
import givers.form.Mappings.{boolean, opt}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}

object UserController {
  case class UpdateData(
    preferredLang: UpdateField[Option[User.PreferredLang]],
    shouldReceiveNewsletter: UpdateField[Boolean]
  )

  val UPDATE_FORM: Form[UpdateData] = Form(
    "validation.login",
    UpdateData.apply,
    Tuples.to[UpdateData],
    "preferredLang" -> UpdateField.form(opt(Helpers.enumForm[User.PreferredLang])),
    "shouldReceiveNewsletter" -> UpdateField.form(boolean)
  )
}

@Singleton
class UserController @Inject() (userService: UserService, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BaseController(cc) {
  import UserController.*

  def edit(): play.api.mvc.Action[AnyContent] = authenticated() { implicit req =>
    Future(Ok(views.html.user.edit(req.loggedInUser)))
  }

  def update(): play.api.mvc.Action[JsValue] = authenticated(parse.json) { implicit req =>
    val data = UPDATE_FORM.bindFromRequest().get

    userService
      .update(
        id = req.loggedInUser.id,
        data = UserService.UpdateData(
          preferredLang = data.preferredLang,
          shouldReceiveNewsletter = data.shouldReceiveNewsletter
        )
      )
      .map { _ =>
        Ok(Json.obj())
      }
  }
}
