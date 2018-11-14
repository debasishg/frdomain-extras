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
  def computeInterest = Kleisli[M, Account, Amount] { (account: Account) =>
    if (account.dateOfClose isDefined) me.raiseError(new IllegalArgumentException(s"Account no ${account.no} is closed"))
    else Account.rate(account).map { r =>
      val a = account.balance.amount
      (a + a * r).pure[M]
    }.getOrElse(BigDecimal(0).pure[M])
  }

  def computeTax = Kleisli[M, Amount, Amount] { (amount: Amount) =>
      (amount * 0.1).pure[M]
  }
}
