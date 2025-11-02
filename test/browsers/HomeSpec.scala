package browsers

import controllers.routes
import modules.ClockService

import java.time.{Clock, Instant, ZoneId}

class HomeSpec extends Base {
  it("tests running the background job") {
    val fixedClock = Clock.fixed(Instant.parse("2020-11-01T00:00:00Z"), ZoneId.systemDefault())
    val clockService = app.injector.instanceOf[ClockService]
    clockService.useFixedClock(fixedClock)

    var user = makeUser(email = "test@test.com", password = Some("1234"))
    user.dummyCounter should be(0)

    setLoggedInUserCookies(user)
    go(routes.HomeController.index().url)

    runAllPendingBackgroundJobs()

    user = await(userService.getById(user.id)).get
    user.dummyCounter should be(1)
  }
}
