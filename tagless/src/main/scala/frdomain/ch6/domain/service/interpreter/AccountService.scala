package frdomain.ch6
package domain
package service
package interpreter

import java.time.LocalDateTime

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import model.{ Account, Balance }
import common._
import repository.AccountRepository

class AccountServiceInterpreter[M[+_]](implicit me: MonadError[M, Throwable]) extends AccountService[M, Account, Amount, Balance] {

  def open(no: String, 
           name: String, 
           rate: Option[BigDecimal],
           openingDate: Option[LocalDateTime],
           accountType: AccountType) = Kleisli[M, AccountRepository[M], Account] { (repo: AccountRepository[M]) =>

    repo.query(no).flatMap(maybeAccount => doOpenAccount(repo, 
      maybeAccount,
      no, 
      name, 
      rate, 
      openingDate, 
      accountType
    ))
  }

  private def doOpenAccount(repo: AccountRepository[M], 
    maybeAccount: Option[Account],
    no: String, 
    name: String, 
    rate: Option[BigDecimal],
    openingDate: Option[LocalDateTime],
    accountType: AccountType): M[Account] = {

    maybeAccount.map(_ => me.raiseError(new IllegalArgumentException(s"Account no $no already exists")))
      .getOrElse(createOrUpdate(repo, no, name, rate, openingDate, accountType))
  }

  private def createOrUpdate(repo: AccountRepository[M],
    no: String, 
    name: String, 
    rate: Option[BigDecimal],
    openingDate: Option[LocalDateTime],
    accountType: AccountType): M[Account] = accountType match {

      case Checking => createOrUpdate(repo, Account.checkingAccount(no, name, openingDate, None, Balance()))
      case Savings  => rate.map(r => createOrUpdate(repo, Account.savingsAccount(no, name, r, openingDate, None, Balance())))
                         .getOrElse(me.raiseError(new IllegalArgumentException("Rate missing for savings account")))
    }

  private def createOrUpdate(repo: AccountRepository[M], 
    errorOrAccount: ErrorOr[Account]): M[Account] = errorOrAccount match {
      case Left(errs) => me.raiseError(new IllegalArgumentException(s"${errs.toList}"))
      case Right(a)   => repo.store(a)
    }

  def close(no: String, closeDate: Option[LocalDateTime]) = Kleisli[M, AccountRepository[M], Account] { (repo: AccountRepository[M]) =>

    for {
      maybeAccount <- repo.query(no)
      account      <- maybeAccount.map(a => createOrUpdate(repo, Account.close(a, closeDate.getOrElse(today))))
                                  .getOrElse(me.raiseError(new IllegalArgumentException(s"Account no $no does not exist")))
    } yield account
  }

  def debit(no: String, amount: Amount) = update(no, amount, D)
  def credit(no: String, amount: Amount) = update(no, amount, C)

  private trait DC
  private case object D extends DC
  private case object C extends DC

  private def update(no: String, amount: Amount, debitCredit: DC) = Kleisli[M, AccountRepository[M], Account] { (repo: AccountRepository[M]) =>
    for {

      maybeAccount   <- repo.query(no)
      multiplier = if (debitCredit == D) (-1) else 1
      account        <- maybeAccount.map(a => createOrUpdate(repo, Account.updateBalance(a, multiplier * amount)))
                                    .getOrElse(me.raiseError(new IllegalArgumentException(s"Account no $no does not exist")))

    } yield account
  }

  def balance(no: String) = Kleisli[M, AccountRepository[M], Balance] { (repo: AccountRepository[M]) =>
    repo.balance(no)
  }

  def transfer(from: String, to: String, amount: Amount): Kleisli[M, AccountRepository[M], (Account, Account)] = for { 
    a <- debit(from, amount)
    b <- credit(to, amount)
  } yield ((a, b))
}
