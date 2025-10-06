package controllers

import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent}
import play.api.{Environment, Mode}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AssetsController @Inject() (
  errorHandler: HttpErrorHandler,
  meta: AssetsMetadata,
  env: Environment
)(implicit ec: ExecutionContext)
    extends Assets(errorHandler, meta, env) {

  override def versioned(path: String, file: Assets.Asset): Action[AnyContent] = Action.async { req =>
    if (
      env.mode == Mode.Dev &&
      (
        file.name.startsWith("svelte_") ||
          file.name.startsWith("svelte/") ||
          file.name.startsWith("stylesheets/tailwindbase.css")
      )
    ) {
      Future(Redirect(s"http://localhost:9001/assets/${file.name}"))
    } else {
      super.versioned(path, file)(req)
    }
  }
}
