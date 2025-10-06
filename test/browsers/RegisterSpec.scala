package browsers

import controllers.routes

class RegisterSpec extends Base {
  it("registers and verifies email") {
    val email = "something2@nanakorn.com"
    go(routes.AuthController.register().url)

    fill(tid("email"), email)
    fill(tid("password"), "1234")
    click(tid("submit-button"))

    waitUntil { getPath() == "/" }

    var user = await(userService.getByEmail(email)).get
    user.isEmailVerified should be(false)

    getLoggedInUserId() should be(Some(user.id))

    val emailVerificationToken = await(emailVerificationTokenService.getByUserId(user.id)).head

    go(routes.AuthController.verifyEmail(user.id, emailVerificationToken.token).url)
    click(tid("submit-button"))

    waitUntil { getPath() == "/" }

    user = await(userService.getByEmail(email)).get
    user.isEmailVerified should be(true)
  }

  it("validates") {
    val user = makeUser()
    go(routes.AuthController.register().url)

    fill(tid("email"), "")
    fill(tid("password"), "")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }

    checkErrorPanel("The email is invalid.", "The password is required.")

    fill(tid("email"), user.email)
    fill(tid("password"), "1234")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }

    checkErrorPanel("The email has previously been registered. Please login.")
  }
}
