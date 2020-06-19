package frdomain.ch6.domain
package io
package app

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.mtl._

import squants.market._

import repository._
import service._
import service.interpreter._
import model.account._
import common._
import programs._

import Implicits._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    config.load[IO].flatMap { cfg =>
      AppResources.make[IO](cfg).use { res =>
        Algebras.make[IO](res.psql).flatMap { algebras =>
          implicit val repositoryAsk = DefaultApplicativeAsk.constant[IO, AccountRepository[IO]](algebras.accountRepository)
          programCreditNonExistingAccount[IO](new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO]).map { result => 
            println(result)
            ExitCode.Success 
          }
        }
      }
    }
}
