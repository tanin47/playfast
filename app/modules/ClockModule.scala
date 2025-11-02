package modules

import com.google.inject.AbstractModule

import java.time.{Clock, Instant}

class ClockService {
  var clock: Clock = Clock.systemUTC()

  def useFixedClock(fixed: Clock): Unit = {
    clock = fixed
  }

  def now(): Instant = {
    Instant.now(clock)
  }
}

class ClockModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[ClockService]).toInstance(new ClockService)
  }
}
