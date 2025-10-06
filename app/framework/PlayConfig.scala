package framework

import play.api.i18n.{Langs, MessagesApi}
import play.api.{ConfigLoader, Configuration, Environment, Logger}

import javax.inject.{Inject, Singleton}

@Singleton
class PlayConfig @Inject() (
  val config: Configuration,
  val env: Environment,
  val messagesApi: MessagesApi,
  val langs: Langs
) {

  private[this] val logger = Logger(getClass)

  val BASE_URL: String = getString("app.baseUrl")
  val MAILGUN_API_KEY: String = getString("mailgun.apiKey")
  val MAILGUN_DOMAIN: String = getString("mailgun.domain")

  def makeFullUrl(path: String): String = s"$BASE_URL$path"

  def getString(key: String): String = {
    getOptString(key)
      .filter(_.nonEmpty)
      .getOrElse {
        logger.error(
          s"The config '$key' doesn't exist in Play conf file or system properties"
        )
        sys.exit(1)
      }
  }

  def getSeq[T](key: String)(implicit loader: ConfigLoader[Seq[T]]): Seq[T] = {
    config.getOptional[Seq[T]](key).getOrElse {
      logger.error(
        s"The config '$key' doesn't exist in Play conf file or system properties"
      )
      sys.exit(1)
    }
  }

  def getInt(key: String): Int = {
    getString(key).toInt
  }

  def getBoolean(key: String): Boolean = {
    getOptString(key).exists(_.toBoolean)
  }

  def getOptString(key: String): Option[String] = {
    Option(System.getProperty(key))
      .orElse(config.getOptional[String](key))
      .map(_.trim)
  }
}
