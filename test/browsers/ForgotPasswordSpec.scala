package browsers

import controllers.routes
import framework.Instant
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class ForgotPasswordSpec extends Base {

  it("resets password end-to-end and consumes token") {
    val oldPassword = "oldpass"
    var user = makeUser(email = "forgot@test.com", password = Some(oldPassword))

    go(routes.AuthController.forgotPassword().url)

    fill(tid("email"), user.email)
    click(tid("submit-button"))
    waitUntil { !hasElem(tid("submit-button")) }

    val forgotPasswordToken = await(forgotPasswordTokenService.getByUserId(user.id)).get

    go(routes.AuthController.resetPassword(user.id, forgotPasswordToken.token).url)

    val newPassword = "newpass"
    fill(tid("password"), newPassword)
    click(tid("submit-button"))

    waitUntil { getPath() == "/" }

    user = await(userService.getById(user.id)).get
    new BCryptPasswordEncoder().matches(newPassword, user.hashedPassword.get) should be(true)
    new BCryptPasswordEncoder().matches(oldPassword, user.hashedPassword.get) should be(false)

    go(routes.AuthController.resetPassword(user.id, forgotPasswordToken.token).url, skipFullLoadedCheck = true)
    elem("body").getText should include("Not Found")
  }

  it("rejects non-existing email and validates input") {
    go(routes.AuthController.forgotPassword().url)

    fill(tid("email"), "")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }
    checkErrorPanel("The email is invalid.")

    fill(tid("email"), "does-not-exist@example.com")
    click(tid("submit-button"))
    waitUntil { elem(tid("submit-button")).getDomProperty("disabled") != "true" }
    checkErrorPanel("The email isn't registered. Please register.")
  }

  it("expires token after 24 hours") {
    val user = makeUser(email = "forgot2@test.com", password = Some("oldpass"))

    go(routes.AuthController.forgotPassword().url)
    fill(tid("email"), user.email)
    click(tid("submit-button"))
    waitUntil { hasElem("a[href='/login']") }

    Instant.advancedTime(hours = 24, seconds = 1)

    val forgotPasswordToken = await(forgotPasswordTokenService.getByUserId(user.id)).get

    go(routes.AuthController.resetPassword(user.id, forgotPasswordToken.token).url, skipFullLoadedCheck = true)
    elem("body").getText should include("Not Found")
  }
}
