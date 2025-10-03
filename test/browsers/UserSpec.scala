package browsers

import controllers.routes
import database.models.User
import org.openqa.selenium.support.ui.Select

class UserSpec extends Base {
  it("updates each field") {
    var user = makeUser(email = "test@test.com", password = Some("1234"))
    user.shouldReceiveNewsletter should be(false)
    user.preferredLang should be(None)

    setLoggedInUserCookies(user)
    go(routes.UserController.edit().url)

    click(tid("receive-newsletter-checkbox"))
    waitUntil { elem(tid("receive-newsletter-checkbox")).getDomAttribute("disabled") != "true" }
    elem(tid("receive-newsletter-checkbox")).getDomProperty("checked") should be("true")
    user = await(userService.getById(user.id)).get
    user.shouldReceiveNewsletter should be(true)

    click(tid("receive-newsletter-checkbox"))
    waitUntil { elem(tid("receive-newsletter-checkbox")).getDomAttribute("disabled") != "true" }
    elem(tid("receive-newsletter-checkbox")).getDomProperty("checked") should be("false")
    user = await(userService.getById(user.id)).get
    user.shouldReceiveNewsletter should be(false)

    val langSelect = new Select(elem(tid("preferred-lang-select")))
    langSelect.selectByValue("Thai")
    waitUntil { elem(tid("preferred-lang-select")).getDomAttribute("disabled") != "true" }
    elem(tid("preferred-lang-select")).getDomProperty("value") should be("Thai")
    user = await(userService.getById(user.id)).get
    user.preferredLang should be(Some(User.PreferredLang.Thai))

    langSelect.selectByValue("")
    waitUntil { elem(tid("preferred-lang-select")).getDomAttribute("disabled") != "true" }
    elem(tid("preferred-lang-select")).getDomProperty("value") should be("")
    user = await(userService.getById(user.id)).get
    user.preferredLang should be(None)
  }
}
