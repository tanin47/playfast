package framework

import java.time.temporal.ChronoUnit
import givers.form.Mapping
import play.api.libs.json.JsLookupResult
import givers.form.BindContext
import scala.util.Try
import play.api.libs.json.JsNumber
import scala.util.Failure
import scala.util.Success
import givers.form.UnbindContext
import play.api.libs.json.{JsValue, JsDefined}

type Instant = java.time.Instant // scalafix:ok

object Instant {
  trait MockedTimeChangeListener {
    def mockedTimeChanged(time: Instant): Unit
  }

  private[this] var mockedTime: Option[Instant] = None
  var mockedTimeChangedListener: Option[MockedTimeChangeListener] = None

  def mockTimeForTest(t: Instant): Unit = {
    mockedTime = Some(t)
    mockedTimeChangedListener.foreach(_.mockedTimeChanged(mockedTime.get))
  }

  def advancedTime(days: Int = 0, hours: Int = 0, minutes: Int = 0, seconds: Int = 0): Unit = {
    mockedTime = Some(
      mockedTime.get
        .plus(days, ChronoUnit.DAYS)
        .plus(hours, ChronoUnit.HOURS)
        .plus(minutes, ChronoUnit.MINUTES)
        .plus(seconds, ChronoUnit.SECONDS)
    )
    mockedTimeChangedListener.foreach(_.mockedTimeChanged(mockedTime.get))
  }

  def ofEpochMillis(millis: Long): Instant = java.time.Instant.ofEpochMilli(millis)

  def now(): Instant = mockedTime
    .getOrElse(
      java.time.Instant.now() // scalafix:ok
    )

  def parse(text: String): Instant = java.time.Instant.parse(text) // scalafix:ok

  def form: Mapping[Instant] = new Mapping[Instant] {
    def bind(value: JsLookupResult, context: BindContext): Try[Instant] = {
      value match {
        case JsDefined(v: JsNumber) => Success(ofEpochMillis(v.value.toLong))
        case _ => Failure(Mapping.error("error.invalid"))
      }
    }

    def unbind(value: Instant, context: UnbindContext): JsValue = throw new UnsupportedOperationException()
  }
}
