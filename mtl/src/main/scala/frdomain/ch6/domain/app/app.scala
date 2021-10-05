package frdomain.ch6
package domain
package io
package app

import scala.collection.immutable.Map 

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._
import cats.effect.IO
import cats.effect.Ref
import cats.mtl._

import squants.market._

import common._
import model.account.{ AccountNo, AccountName, Account, Balance, AccountType }
import AccountType._
import repository.AccountRepository
import repository.interpreter.AccountRepositoryInMemory
import service.{ ReportingService, AccountService }
import service.interpreter._
import cats.effect._

object App extends IOApp.Simple {

  def run: IO[Unit] = {
    IO {
      List(UseCase1(), UseCase2(), UseCase3(), UseCase4()).foreach {
        _.flatMap { vals => IO(vals.foreach(println)) }
      }
    }

    // [info] List((a5678,0 USD), (a3456,3E+3 USD), (a1234,1E+3 USD), (a2345,2E+3 USD), (a4567,4E+3 USD))
    // [info] No existing account with no a2345
    // [info] Insufficient amount in a1234 to debit
    // [info] Account No has to be at least 5 characters long: found a134/Interest rate -0.9 must be > 0
  }

  object UseCase1 {

    def apply() = { 
      import Implicits._
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        implicit val repositoryAsk = DefaultApplicativeAsk.constant[IO, AccountRepository[IO]](repo)
        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
      (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

      import accountService._
      import reportingService._

      val opens = 
        for {
          _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
          _ <- open(AccountNo("a2345"), AccountName("a2name"), None, None, Checking)
          _ <- open(AccountNo("a3456"), AccountName("a3name"), BigDecimal(5.8).some, None, Savings)
          _ <- open(AccountNo("a4567"), AccountName("a4name"), None, None, Checking)
          _ <- open(AccountNo("a5678"), AccountName("a5name"), BigDecimal(2.3).some, None, Savings)
        } yield (())
    
      val credits = 
        for {
          _ <- credit(AccountNo("a1234"), USD(1000))
          _ <- credit(AccountNo("a2345"), USD(2000))
          _ <- credit(AccountNo("a3456"), USD(3000))
          _ <- credit(AccountNo("a4567"), USD(4000))
        } yield (())
    
      for {
        _ <- opens
        _ <- credits
        a <- balanceByAccount
      } yield a
    }
  }

  object UseCase2 {

    def apply() = { 
      import Implicits._
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        implicit val repositoryAsk = DefaultApplicativeAsk.constant[IO, AccountRepository[IO]](repo)
        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
      (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

      import accountService._
      import reportingService._

      for {
        _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
        _ <- credit(AccountNo("a2345"), USD(2000))
        a <- balanceByAccount
      } yield a
    }
  }

  object UseCase3 {

    def apply() = { 
      import Implicits._
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        implicit val repositoryAsk = DefaultApplicativeAsk.constant[IO, AccountRepository[IO]](repo)
        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
      (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

      import accountService._
      import reportingService._

      for {
        _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
        _ <- credit(AccountNo("a1234"), USD(2000))
        _ <- debit(AccountNo("a1234"), USD(4000))
        a <- balanceByAccount
      } yield a
    }
  }

  object UseCase4 {

    def apply() = { 
      import Implicits._
      AccountRepositoryInMemory.make[IO].flatMap { repo =>
        implicit val repositoryAsk = DefaultApplicativeAsk.constant[IO, AccountRepository[IO]](repo)
        program(new AccountServiceInterpreter[IO], new ReportingServiceInterpreter[IO])
      }
    }

    def program[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
      (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

      import accountService._
      import reportingService._

      for {
        a <- open(AccountNo("a134"), AccountName("a1name"), Some(BigDecimal(-0.9)), None, Savings)
        _ <- credit(a.no, USD(2000))
        _ <- debit(a.no, USD(4000))
        b <- balanceByAccount
      } yield b
    }
  }
}
