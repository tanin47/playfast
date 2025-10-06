package browsers

import controllers.routes

class LoginSpec extends Base {

  it("logins correctly") {
    val user = makeUser(password = Some("1234"))
    go(routes.AuthController.login().url)

    fill(tid("email"), user.email)
    fill(tid("password"), "1234")
    click(tid("submit-button"))

    waitUntil { getPath() == "/" }

    getLoggedInUserId() should be(Some(user.id))
  }

  it("validates") {
    val user = makeUser(email = "test@test.com", password = Some("1234"))
    go(routes.AuthController.login().url)

    fill(tid("email"), user.email)
    fill(tid("password"), "12346")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }

    checkErrorPanel("The password is incorrect.")

    fill(tid("email"), "")
    fill(tid("password"), "")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }

    checkErrorPanel("The email is invalid.", "The password is required.")

    fill(tid("email"), "some@nanakorn.com")
    fill(tid("password"), "123")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }

    checkErrorPanel("The email isn't registered. Please register.")

    getLoggedInUserId() should be(None)
  }
}
