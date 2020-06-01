package frdomain.ch6
package domain
package service

import java.util.Date
import zio._
import repository.AccountRepository
import common._
import model.{Account, Balance}
import cats.data.NonEmptyList

sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType

object AccountService {
  trait Service {

    def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
      accountType: AccountType): IO[AccountServiceException, Account]
    def close(no: String, closeDate: Option[Date]): IO[AccountServiceException, Account]
    def debit(no: String, amount: Amount): IO[AccountServiceException, Account]
    def credit(no: String, amount: Amount): IO[AccountServiceException, Account]
    def balance(no: String): IO[AccountServiceException, Balance]
    def transfer(from: String, to: String, amount: Amount): IO[AccountServiceException, (Account, Account)]
  }

  val live = ZLayer.fromService[AccountRepository.Service, AccountService.Service] {
    (repo: AccountRepository.Service) =>

    new Service {

      def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
        accountType: AccountType): IO[AccountServiceException, Account] =

        withAccountServiceException(repo.query(no)).flatMap(maybeAccount => doOpenAccount(
          maybeAccount,
          no, 
          name, 
          rate, 
          openingDate, 
          accountType
        )) 

      private def doOpenAccount(
        maybeAccount: Option[Account],
        no: String, 
        name: String, 
        rate: Option[BigDecimal],
        openingDate: Option[Date],
        accountType: AccountType): IO[AccountServiceException, Account] = {

        maybeAccount.map(_ => IO.fail(AlreadyExistingAccount(no)))
          .getOrElse(createOrUpdate(no, name, rate, openingDate, accountType))
      }

      private def createOrUpdate(
        no: String, 
        name: String, 
        rate: Option[BigDecimal],
        openingDate: Option[Date],
        accountType: AccountType): IO[AccountServiceException, Account] = accountType match {

          case Checking => createOrUpdate(Account.checkingAccount(no, name, openingDate, None, Balance()))
          case Savings  => rate.map(r => createOrUpdate(Account.savingsAccount(no, name, r, openingDate, None, Balance())))
                               .getOrElse(IO.fail(RateMissingForSavingsAccount))
        }

      private def createOrUpdate(errorOrAccount: ErrorOr[Account]): IO[AccountServiceException, Account] = errorOrAccount match {
        case Left(errs) => IO.fail(MiscellaneousDomainExceptions(errs))
        case Right(a)   => withAccountServiceException(repo.store(a))
      }

      def close(no: String, closeDate: Option[Date]): IO[AccountServiceException, Account] =
        withAccountServiceException(repo.query(no)).flatMap {
          case Some(a) => createOrUpdate(Account.close(a, closeDate.getOrElse(today)))
          case None    => IO.fail(NonExistingAccount(no)) 
        }

      def debit(no: String, amount: Amount): IO[AccountServiceException, Account] = update(no, amount, D)
      def credit(no: String, amount: Amount): IO[AccountServiceException, Account] = update(no, amount, C)

      private trait DC
      private case object D extends DC
      private case object C extends DC

      private def update(no: String, amount: Amount, debitCredit: DC): IO[AccountServiceException, Account] = {
        val multiplier = if (debitCredit == D) (-1) else 1 
      
        withAccountServiceException(repo.query(no)).flatMap {
          case Some(a) => createOrUpdate(Account.updateBalance(a, multiplier * amount))
          case None    => IO.fail(NonExistingAccount(no)) 
        }
      }

      def balance(no: String): IO[AccountServiceException, Balance] = 
        withAccountServiceException(repo.query(no)).flatMap {
          case Some(a) => IO.succeed(a.balance)
          case None    => IO.fail(NonExistingAccount(no))
        }

      def transfer(from: String, to: String, amount: Amount): IO[AccountServiceException, (Account, Account)] = for { 
        a <- debit(from, amount)
        b <- credit(to, amount)
      } yield ((a, b))

      private def withAccountServiceException[A](t: Task[A]): IO[AccountServiceException, A] = 
        t.foldM(
          error   => IO.fail(MiscellaneousDomainExceptions(NonEmptyList.of(error.getMessage))),
          success => IO.succeed(success)
        )

    }
  }
}
