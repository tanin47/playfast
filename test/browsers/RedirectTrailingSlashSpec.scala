package browsers

import controllers.routes

class RedirectTrailingSlashSpec extends Base {

  it("redirects trailing slash to non-trailing slash") {
    go(routes.AuthController.login().url + "/")

    waitUntil { getPath() == routes.AuthController.login().url }
  }

  it("redirects trailing slash to non-trailing slash with query string") {
    go(routes.AuthController.login().url + "/?test=1")

    waitUntil { getPath() == (routes.AuthController.login().url + "?test=1") }
  }
}
