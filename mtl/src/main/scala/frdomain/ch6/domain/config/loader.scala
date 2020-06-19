package frdomain.ch6.domain
package config

import cats.effect._
import cats.implicits._
import ciris._
import ciris.refined._
import environments._
import environments.AppEnvironment._
import eu.timepit.refined.auto._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import config._

object load {
  
  def apply[F[_]: Async: ContextShift]: F[AppConfig] =
    env("BANKING_APP_ENV")
      .as[AppEnvironment]
      .default(Prod)
      .flatMap {
        case Test => default
        case Prod => default
      }
      .load[F]

  private def default: ConfigValue[AppConfig] =
    (
      env("DATABASE_USER").as[NonEmptyString].default("postgres"),
      env("DATABASE_NAME").as[NonEmptyString].default("banking"),
    ).parMapN { (dbuser, dbname) =>
      AppConfig(
        PostgreSQLConfig(
          host = "localhost",
          port = 5432,
          user = dbuser,
          database = dbname,
          max = 10
        )
      )
    }
}
