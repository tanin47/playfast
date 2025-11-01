package framework

import java.time.temporal.ChronoUnit

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

  def now(): Instant = mockedTime
    .getOrElse(
      java.time.Instant.now() // scalafix:ok
    )

  def parse(text: String): Instant = java.time.Instant.parse(text) // scalafix:ok
}
