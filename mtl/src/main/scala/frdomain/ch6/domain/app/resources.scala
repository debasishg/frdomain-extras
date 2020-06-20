package frdomain.ch6
package domain
package io
package app

import cats.effect._
import cats.implicits._

import skunk._
import skunk.util.Typer
import natchez.Trace.Implicits.noop // needed for skunk

import config.config._

final case class AppResources[F[_]](
  psql: Resource[F, Session[F]]
)

object AppResources {

  def make[F[_]: ConcurrentEffect: ContextShift](
      cfg: AppConfig
  ): Resource[F, AppResources[F]] = {

    def mkPostgreSqlResource(c: PostgreSQLConfig): SessionPool[F] =
      Session
        .pooled[F](
          host = c.host.value,
          port = c.port.value,
          user = c.user.value,
          database = c.database.value,
          max = c.max.value,
          strategy = Typer.Strategy.SearchPath
        )

    mkPostgreSqlResource(cfg.postgreSQL).map(AppResources.apply[F])
  }
}