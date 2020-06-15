package frdomain.ch6
package domain
package service
package interpreter

import java.util.{ Date, Calendar }

import cats._
import cats.implicits._
import cats.mtl._

import model.account._
import common._
import repository.AccountRepository

class AccountServiceInterpreter[M[+_]]
  (implicit E: MonadError[M, AppException], A: ApplicativeAsk[M, AccountRepository[M]]) 
    extends AccountService[M, Account, Amount, Balance] {

  def open(no: AccountNo, 
    name: AccountName, 
    rate: Option[BigDecimal],
    openingDate: Option[Date],
    accountType: AccountType): M[Account] = {

    for {

      repo           <- A.ask
      maybeAccount   <- repo.query(no)
      account        <- doOpenAccount(repo, maybeAccount, no, name, rate, openingDate, accountType)

    } yield account
  }

  private def doOpenAccount(repo: AccountRepository[M], 
    maybeAccount: Option[Account],
    no: AccountNo, 
    name: AccountName, 
    rate: Option[BigDecimal],
    openingDate: Option[Date],
    accountType: AccountType): M[Account] = {

    maybeAccount.map(_ => E.raiseError(AlreadyExistingAccount(no)))
      .getOrElse(createOrUpdate(repo, no, name, rate, openingDate, accountType))
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
      maybeAccount   <- repo.query(no)
      account        <- maybeAccount.map(a => createOrUpdate(repo, Account.close(a, closeDate.getOrElse(today))))
                                    .getOrElse(E.raiseError(NonExistingAccount(no)))
    } yield account
  }

  def balance(no: AccountNo): M[Balance] = for {

    repo  <- A.ask
    b     <- repo.balance(no)

  } yield b

  private trait DC
  private object D extends DC
  private object C extends DC

  def debit(no: AccountNo, amount: Amount): M[Account] = update(no, amount, D)
  def credit(no: AccountNo, amount: Amount): M[Account] = update(no, amount, C)

  private def update(no: AccountNo, amount: Amount, debitCredit: DC): M[Account] = for {

    repo           <- A.ask
    maybeAccount   <- repo.query(no)
    multiplier = if (debitCredit == D) (-1) else 1
    account        <- maybeAccount.map(a => createOrUpdate(repo, Account.updateBalance(a, multiplier * amount)))
                                  .getOrElse(E.raiseError(NonExistingAccount(no)))

  } yield account

  def transfer(from: AccountNo, to: AccountNo, amount: Amount): M[(Account, Account)] = for { 
    a <- debit(from, amount)
    b <- credit(to, amount)
  } yield ((a, b))
}

