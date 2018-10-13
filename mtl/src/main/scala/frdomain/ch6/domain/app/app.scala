package frdomain.ch6
package domain
package io
package app

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._
import cats.effect.IO
import cats.mtl._

import service.interpreter.AccountServiceInterpreter
import repository.AccountRepository
import repository.interpreter.AccountRepositoryInMemory
import service.{ Checking, Savings }
import common._

object App {
  import Implicits._

  val repo = new AccountRepositoryInMemory[IO]

  implicit val repositoryAsk = DefaultApplicativeAsk.constant[IO, AccountRepository[IO]](repo)
  val accountServiceIO = new AccountServiceInterpreter[IO]

  import accountServiceIO._

  def main(args: Array[String]): Unit = {

    val bal = for {

      a <- open("a1234", "a1name", None, None, Checking)
      _ <- credit("a1234", 2000.00)
      _ <- debit("a1234", 1000.00)
      _ <- credit("a1234", 5000.00)
      b <- balance(a.no)

    } yield b

    println(bal.unsafeRunSync)
  }
}
