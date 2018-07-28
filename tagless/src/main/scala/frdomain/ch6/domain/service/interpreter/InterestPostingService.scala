package frdomain.ch6
package domain
package service
package interpreter

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import common._
import model.Account

class InterestPostingServiceInterpreter[M[+_]](implicit me: MonadError[M, Throwable]) extends InterestPostingService[M, Account, Amount] {
  def computeInterest = Kleisli[Valid[M, ?], Account, Amount] { (account: Account) =>
    EitherT {
      if (account.dateOfClose isDefined) Left(ClosedAccount(account.no)).pure[M]
      else Account.rate(account).map { r =>
        val a = account.balance.amount
        (Right(a + a * r)).pure[M]
      }.getOrElse(Right(BigDecimal(0)).pure[M])
    }
  }

  def computeTax = Kleisli[Valid[M, ?], Amount, Amount] { (amount: Amount) =>
    EitherT[M, AccountServiceException, Amount] {
      Right(amount * 0.1).pure[M]
    }
  }
}
