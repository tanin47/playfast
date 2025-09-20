package browsers

import base.Base.IS_MAC
import database.models.User
import framework.Instant.MockedTimeChangeListener
import framework.{BaseController, Instant}
import org.openqa.selenium.chrome.{ChromeDriver, ChromeOptions}
import org.openqa.selenium.logging.{LogType, LoggingPreferences}
import org.openqa.selenium.*
import play.api.mvc.{DefaultSessionCookieBaker, Session}
import play.api.test.TestServer

import java.util.logging.Level
import scala.jdk.CollectionConverters.ListHasAsScala

object Base {
  lazy val webDriver: ChromeDriver = {
    val options = new ChromeOptions()
    if (sys.env.get("HEADLESS").contains("true")) {
      options.addArguments("--headless")
      println("Running Chrome in the headless mode")
    }

    options.addArguments("--disable-extensions")
    options.addArguments("--disable-web-security")
    options.addArguments("--window-size=1280,800")
    options.addArguments("--disable-dev-shm-usage")
    options.addArguments("--disable-smooth-scrolling")

    val logPrefs = new LoggingPreferences()
    logPrefs.enable(LogType.BROWSER, Level.ALL)
    options.setCapability("goog:loggingPrefs", logPrefs)

    new ChromeDriver(options)
  }
}

trait Base extends base.Base with MockedTimeChangeListener {
  import Base.*
  val testServer: TestServer = {
    val s = TestServer(
      port = base.Base.PORT,
      application = base.Base.app
    )
    s.start()

    s
  }

  def mockedTimeChanged(time: Instant): Unit = {
    webDriver.executeScript(
      s"""
         |if (!window.OriginalDate) {
         |  window.OriginalDate = Date;
         |}
         |
         |Date = function(...args) {
         |  if (args.length === 0) {
         |    return new window.OriginalDate(${time.toEpochMilli});
         |  } else {
         |    return new window.OriginalDate(...args);
         |  }
         |};
         |""".stripMargin
    )
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    go("/")
    Instant.mockedTimeChangedListener = Some(this)
    webDriver.manage().deleteAllCookies()
  }

  override def afterAll(): Unit = {
    Instant.mockedTimeChangedListener = None
    webDriver.close()
    super.afterAll()
  }

  def go(pathOrUrl: String): Unit = {
    webDriver.get(s"http://localhost:${base.Base.PORT}$pathOrUrl")
    waitUntil {
      try {
        val loaded = webDriver.executeScript("return IS_PAGE_FULLY_LOADED_FOR_TEST")
        loaded != null && loaded.asInstanceOf[Boolean]
      } catch { case _: JavascriptException => false }
    }
    mockedTimeChanged(Instant.now())
  }

  def clearLoggedInUserCookies(): Unit = {
    go("/")

    webDriver.manage().deleteAllCookies()
  }

  def setLoggedInUserCookies(user: User): Unit = {
    go("/")

    val sessionCookieBaker = app.injector.instanceOf[DefaultSessionCookieBaker]

    val cookies = Seq(
      sessionCookieBaker.encodeAsCookie(
        new Session(
          Map(
            BaseController.USER_ID_SESSION_KEY -> user.id
          )
        )
      )
    )

    cookies
      .foreach { cookie =>
        webDriver
          .manage()
          .addCookie(new Cookie(cookie.name, cookie.value))
      }
  }

  // See why: https://tanin.nanakorn.com/set-up-intellij-to-run-scalatests-funspec/
  def it(name: String, user: => User)(fn: => Any): Unit = it(name) {
    setLoggedInUserCookies(user)

    fn
  }

  def fill(cssSelector: String, text: String): Unit = {
    val el = elem(cssSelector)

    el.sendKeys(
      if (IS_MAC) { Keys.COMMAND }
      else { Keys.CONTROL },
      "a"
    )
    el.sendKeys(Keys.BACK_SPACE)

    Thread.sleep(10)
    el.sendKeys(text)
  }

  def tid(dataTestId: String): String = s"[data-test-id='$dataTestId']"

  def click(cssSelector: String): Unit = {
    val el = elem(cssSelector)
    el.click()
  }

  private[this] def getElem(cssSelector: String, checkDisplay: Boolean): Option[WebElement] = {
    val elems = webDriver.findElements(By.cssSelector(cssSelector)).asScala.toList

    if (checkDisplay) {
      elems.find(_.isDisplayed)
    } else {
      elems.headOption
    }
  }

  def elem(cssSelector: String, checkDisplay: Boolean = true): WebElement = {
    waitUntil { getElem(cssSelector, checkDisplay).isDefined }
    getElem(cssSelector, checkDisplay).get
  }

  def elems(cssSelector: String, checkDisplay: Boolean = true): Seq[WebElement] = {
    waitUntil { getElem(cssSelector, checkDisplay).isDefined }
    webDriver.findElements(By.cssSelector(cssSelector)).asScala.toList
  }

  def getUrl(): String = {
    webDriver.getCurrentUrl
  }

  def getPath(): String = {
    getUrl().substring(s"http://localhost:${base.Base.PORT}".length)
  }

  def getLoggedInUserId(): Option[String] = {
    val result = webDriver.executeScript("""
                                           |if (LOGGED_IN_USER) {
                                           |  return LOGGED_IN_USER.id
                                           |} else {
                                           |  return null
                                           |}
                                           |""".stripMargin)
    Option(result.asInstanceOf[String])
  }
}
