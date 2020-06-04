package frdomain.ch6.domain

import pureconfig.ConfigConvert
import pureconfig.ConfigSource
import pureconfig.generic.semiauto._
import zio.{ Has, ZIO, ZLayer }

object config {
  final case class Config(
    appConfig: AppConfig,
    dbConfig: DBConfig
  )

  object Config {
    implicit val convert: ConfigConvert[Config] = deriveConvert
  }

  type ConfigProvider = Has[Config]

  object ConfigProvider {

    val live: ZLayer[Any, IllegalStateException, Has[Config]] =
      ZLayer.fromEffect {
        ZIO
          .fromEither(ConfigSource.default.load[Config])
          .mapError(
            failures =>
              new IllegalStateException(
                s"Error loading configuration: $failures"
              )
          )
      }
  }

  type DbConfigProvider = Has[DBConfig]
  type AppConfigProvider = Has[AppConfig]

  object DbConfigProvider {

    val fromConfig: ZLayer[ConfigProvider, Nothing, DbConfigProvider] =
      ZLayer.fromService(_.dbConfig)
  }

  object AppConfigProvider {

    val fromConfig: ZLayer[ConfigProvider, Nothing, AppConfigProvider] =
      ZLayer.fromService(_.appConfig)
  }

  final case class AppConfig(
    maxAccountNoLength: Int,
    minAccountNoLength: Int,
    zeroBalanceAllowed: Boolean
  )

  object AppConfig {
    implicit val convert: ConfigConvert[AppConfig] = deriveConvert
  }

  final case class DBConfig(
    url: String,
    driver: String,
    user: String,
    password: String
  )
  object DBConfig {
    implicit val convert: ConfigConvert[DBConfig] = deriveConvert
  }
  
}
