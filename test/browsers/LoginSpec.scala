package browsers

import controllers.routes

class LoginSpec extends Base {

  it("logins correctly") {
    val user = makeUser(email = "test@test.com", password = "1234")
    go(routes.AuthController.login().url)

    fill(tid("email"), user.email)
    fill(tid("password"), "1234")
    click(tid("submit-button"))

    waitUntil { getPath() == "/" }

    getLoggedInUserId() should be(Some(user.id))
  }
}
