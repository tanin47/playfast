package browsers

import controllers.routes

class HomeSpec extends Base {
  it("tests running the background job") {
    var user = makeUser(email = "test@test.com", password = Some("1234"))
    user.dummyCounter should be(0)

    setLoggedInUserCookies(user)
    go(routes.HomeController.index().url)

    runAllPendingBackgroundJobs()

    user = await(userService.getById(user.id)).get
    user.dummyCounter should be(1)
  }
}
