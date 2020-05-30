package frdomain.ch6
package domain
package service

import java.util.Date
import zio._
import repository.AccountRepository
import common._
import model.{Account, Balance}

sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType

object AccountService {
  trait Service {

    def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
      accountType: AccountType): UIO[Account]
    def close(no: String, closeDate: Option[Date]): UIO[Account]
    def debit(no: String, amount: Amount): UIO[Account]
    def credit(no: String, amount: Amount): UIO[Account]
    def balance(no: String): UIO[Balance]
    def transfer(from: String, to: String, amount: Amount): UIO[(Account, Account)]
  }

  val live = ZLayer.fromService[AccountRepository.Service, AccountService.Service] {
    (repo: AccountRepository.Service) =>

    new Service {

      def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
        accountType: AccountType): UIO[Account] = 
        repo.query(no).flatMap(maybeAccount => doOpenAccount(
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
        accountType: AccountType): UIO[Account] = {

        maybeAccount.map(_ => ZIO.die(new IllegalArgumentException(s"Account no $no already exists")))
          .getOrElse(createOrUpdate(no, name, rate, openingDate, accountType))
      }

      private def createOrUpdate(
        no: String, 
        name: String, 
        rate: Option[BigDecimal],
        openingDate: Option[Date],
        accountType: AccountType): UIO[Account] = accountType match {

          case Checking => createOrUpdate(Account.checkingAccount(no, name, openingDate, None, Balance()))
          case Savings  => rate.map(r => createOrUpdate(Account.savingsAccount(no, name, r, openingDate, None, Balance())))
                             .getOrElse(ZIO.die(new IllegalArgumentException("Rate missing for savings account")))
        }

      private def createOrUpdate(errorOrAccount: ErrorOr[Account]): UIO[Account] = errorOrAccount match {
          case Left(errs) => ZIO.die(new IllegalArgumentException(s"${errs.toList}"))
          case Right(a)   => repo.store(a)
        }

      def close(no: String, closeDate: Option[Date]): UIO[Account] = for {
        maybeAccount <- repo.query(no)
        account      <- maybeAccount.map(a => createOrUpdate(Account.close(a, closeDate.getOrElse(today))))
                                    .getOrElse(ZIO.die(new IllegalArgumentException(s"Account no $no does not exist")))
      } yield account

      def debit(no: String, amount: Amount): UIO[Account] = update(no, amount, D)
      def credit(no: String, amount: Amount): UIO[Account] = update(no, amount, C)

      private trait DC
      private case object D extends DC
      private case object C extends DC

      private def update(no: String, amount: Amount, debitCredit: DC): UIO[Account] = for {

          maybeAccount   <- repo.query(no)
          multiplier = if (debitCredit == D) (-1) else 1
          account        <- maybeAccount.map(a => createOrUpdate(Account.updateBalance(a, multiplier * amount)))
                                        .getOrElse(ZIO.die(new IllegalArgumentException(s"Account no $no does not exist")))

        } yield account

      def balance(no: String): UIO[Balance] = repo.balance(no)

      def transfer(from: String, to: String, amount: Amount): UIO[(Account, Account)] = for { 
        a <- debit(from, amount)
        b <- credit(to, amount)
      } yield ((a, b))
    }
  }
}
