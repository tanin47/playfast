package database

import base.Base
import ch.qos.logback.classic.Level
import org.slf4j.LoggerFactory
import play.api.db.DBApi
import play.api.db.evolutions.{DefaultEvolutionsApi, Evolutions}

class DownScriptSpec extends Base {
  before {
    LoggerFactory
      .getLogger("play.api.db.evolutions")
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .setLevel(Level.DEBUG)
  }

  after {
    LoggerFactory
      .getLogger("play.api.db.evolutions")
      .asInstanceOf[ch.qos.logback.classic.Logger]
      .setLevel(Level.INFO)
  }

  it("runs up and down script to ensure they are both valid") {
    val dbApi = app.injector.instanceOf[DBApi]
    Evolutions.cleanupEvolutions(dbApi.database("default"))
    val evo = app.injector.instanceOf[DefaultEvolutionsApi]
    evo.applyFor("default")
  }
}
