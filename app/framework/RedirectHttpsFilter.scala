package framework

import play.api.mvc.{EssentialAction, EssentialFilter}
import play.filters.https.RedirectHttpsFilter as BaseRedirectHttpsFilter

import javax.inject.Inject

class RedirectHttpsFilter @Inject()(base: BaseRedirectHttpsFilter) extends EssentialFilter {
  override def apply(next: EssentialAction): EssentialAction = { req =>
    if (req.path.startsWith("/.well-known/acme-challenge")) {
      next.apply(req)
    } else {
      base.apply(next).apply(req)
    }
  }
}
