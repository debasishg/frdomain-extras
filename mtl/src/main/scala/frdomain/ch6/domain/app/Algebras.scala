package frdomain.ch6
package domain
package io
package app

import cats.Parallel
import cats.effect._
import cats.implicits._

import skunk._

import repository._
import repository.interpreter._

object Algebras {

  def make[F[_]: Sync](
    sessionPool: Resource[F, Session[F]]
  ): F[Algebras[F]] = 
  for {
    accountRepositoryInterpreter <- AccountRepositorySkunk.make[F](sessionPool)
  } yield new Algebras[F](accountRepositoryInterpreter)
}

final class Algebras[F[_]] (
  val accountRepository: AccountRepository[F]
)
