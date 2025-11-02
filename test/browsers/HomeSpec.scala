package browsers

import controllers.routes
import org.mockito.Mockito
import org.mockito.Mockito.{mockStatic, when}

import java.time.{Clock, Instant}

class HomeSpec extends Base {
  it("tests running the background job") {
    val fixedInstant = Instant.parse("2024-11-01T10:00:00Z")
    val mockedInstant = mockStatic(classOf[Instant], Mockito.CALLS_REAL_METHODS)
    when(Instant.now()).thenReturn(fixedInstant)
    println("In test: " + Instant.now())

    var user = makeUser(email = "test@test.com", password = Some("1234"))
    user.dummyCounter should be(0)

    setLoggedInUserCookies(user)
    go(routes.HomeController.index().url)

    runAllPendingBackgroundJobs()

    user = await(userService.getById(user.id)).get
    user.dummyCounter should be(1)

    val now = Instant.now()
    now should be(fixedInstant)
  }
}
