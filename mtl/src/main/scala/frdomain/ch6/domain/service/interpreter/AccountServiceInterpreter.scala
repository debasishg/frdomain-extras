package frdomain.ch6
package domain
package service
package interpreter

import java.util.{ Date, Calendar }

import cats._
import cats.implicits._
import cats.mtl._
import squants.market._

import model.account._
import common._
import repository.AccountRepository

class AccountServiceInterpreter[M[+_]]
  (implicit E: MonadError[M, AppException], 
            A: ApplicativeAsk[M, AccountRepository[M]]) extends AccountService[M, Account, Amount, Balance] {

  def open(no: AccountNo, 
    name: AccountName, 
    rate: Option[BigDecimal],
    openingDate: Option[Date],
    accountType: AccountType): M[Account] = {

    for {

      repo           <- A.ask
      _              <- repo.query(no).ensureOr(_ => AlreadyExistingAccount(no))(!_.isDefined) 
      account        <- createOrUpdate(repo, no, name, rate, openingDate, accountType)

    } yield account
  }

  private def createOrUpdate(repo: AccountRepository[M],
    no: AccountNo, 
    name: AccountName, 
    rate: Option[BigDecimal],
    openingDate: Option[Date],
    accountType: AccountType): M[Account] = accountType match {

      case Checking => createOrUpdate(repo, Account.checkingAccount(no, name, openingDate, None, Balance()))
      case Savings  => rate.map(r => createOrUpdate(repo, Account.savingsAccount(no, name, r, openingDate, None, Balance())))
                           .getOrElse(E.raiseError(RateMissingForSavingsAccount))
    }

  private def createOrUpdate(repo: AccountRepository[M], 
    errorOrAccount: ErrorOr[Account]): M[Account] = errorOrAccount match {
      case Left(errs) => E.raiseError(MiscellaneousDomainExceptions(errs))
      case Right(a)   => repo.store(a)
    }

  def close(no: AccountNo, closeDate: Option[Date]): M[Account] = {

    for {

      repo           <- A.ask
      maybeAccount   <- repo.query(no).ensureOr(_ => NonExistingAccount(no))(_.isDefined) 
      account        <- createOrUpdate(repo, Account.close(maybeAccount.get, closeDate.getOrElse(today)))

    } yield account
  }

  def balance(no: AccountNo): M[Option[Balance]] = for {

    repo  <- A.ask
    b     <- repo.balance(no)

  } yield b

  private trait DC
  private object D extends DC
  private object C extends DC

  def debit(no: AccountNo, amount: Money): M[Account] = update(no, amount, D)
  def credit(no: AccountNo, amount: Money): M[Account] = update(no, amount, C)

  private def update(no: AccountNo, amount: Money, debitCredit: DC): M[Account] = for {

    repo           <- A.ask
    maybeAccount   <- repo.query(no).ensureOr(_ => NonExistingAccount(no))(_.isDefined) 
    multiplier = if (debitCredit == D) (-1) else 1
    account        <- createOrUpdate(repo, Account.updateBalance(maybeAccount.get, multiplier * amount))

  } yield account

  def transfer(from: AccountNo, to: AccountNo, amount: Money): M[(Account, Account)] = for { 
    a <- debit(from, amount)
    b <- credit(to, amount)
  } yield ((a, b))
}

