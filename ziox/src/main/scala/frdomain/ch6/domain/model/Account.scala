package frdomain.ch6
package domain
package model

import java.util.{ Date, Calendar }

import cats._
import cats.data._
import cats.syntax.all._

import common._

case class Balance(amount: Amount = 0)

sealed trait Account {
  def no: String
  def name: String
  def dateOfOpen: Option[Date]
  def dateOfClose: Option[Date]
  def balance: Balance
}

final case class CheckingAccount (no: String, name: String,
  dateOfOpen: Option[Date], dateOfClose: Option[Date] = None, balance: Balance = Balance()) extends Account

final case class SavingsAccount (no: String, name: String, rateOfInterest: Amount, 
  dateOfOpen: Option[Date], dateOfClose: Option[Date] = None, balance: Balance = Balance()) extends Account

object Account {

  private def validateAccountNo(no: String): ValidationResult[String] = 
    if (no.isEmpty || no.size < 5) s"Account No has to be at least 5 characters long: found $no".invalidNec
    else no.validNec

  private def validateOpenCloseDate(od: Date, cd: Option[Date]): ValidationResult[(Option[Date], Option[Date])] = cd.map { c => 
    if (c before od) s"Close date [$c] cannot be earlier than open date [$od]".invalidNec
    else (od.some, cd).validNec
  }.getOrElse { (od.some, cd).validNec }

  private def validateRate(rate: BigDecimal): ValidationResult[BigDecimal] =
    if (rate <= BigDecimal(0)) s"Interest rate $rate must be > 0".invalidNec
    else rate.validNec

  def checkingAccount(no: String, name: String, openDate: Option[Date], closeDate: Option[Date], 
    balance: Balance): ErrorOr[Account] = { 

    ( 
      validateAccountNo(no),
      validateOpenCloseDate(openDate.getOrElse(today), closeDate)

    ).mapN {(n, d) =>
      CheckingAccount(n, name, d._1, d._2, balance)
    }.toEither
  }

  def savingsAccount(no: String, name: String, rate: BigDecimal, openDate: Option[Date], 
    closeDate: Option[Date], balance: Balance): ErrorOr[Account] = { 

    (
      validateAccountNo(no),
      validateOpenCloseDate(openDate.getOrElse(today), closeDate),
      validateRate(rate)

    ).mapN {(n, d, r) =>
      SavingsAccount(n, name, r, d._1, d._2, balance)
    }.toEither
  }

  private def validateAccountAlreadyClosed(a: Account): ValidationResult[Account] = {
    if (a.dateOfClose isDefined) s"Account ${a.no} is already closed".invalidNec
    else a.validNec
  }

  private def validateCloseDate(a: Account, cd: Date): ValidationResult[Date] = {
    if (cd before a.dateOfOpen.get) s"Close date [$cd] cannot be earlier than open date [${a.dateOfOpen.get}]".invalidNec
    else cd.validNec
  }

  def close(a: Account, closeDate: Date): ErrorOr[Account] = {
    (validateAccountAlreadyClosed(a), validateCloseDate(a, closeDate)).mapN {(acc, _) =>
      acc match {
        case c: CheckingAccount => c.copy(dateOfClose = Some(closeDate))
        case s: SavingsAccount  => s.copy(dateOfClose = Some(closeDate))
      }
    }.toEither
  }

  private def checkBalance(a: Account, amount: Amount): ValidationResult[Account] = {
    if (amount < 0 && a.balance.amount < -amount) s"Insufficient amount in ${a.no} to debit".invalidNec
    else a.validNec
  }

  def updateBalance(a: Account, amount: Amount): ErrorOr[Account] = {
    (validateAccountAlreadyClosed(a), checkBalance(a, amount)).mapN { (_, _) =>
      a match {
        case c: CheckingAccount => c.copy(balance = Balance(c.balance.amount + amount))
        case s: SavingsAccount  => s.copy(balance = Balance(s.balance.amount + amount))
      }
    }.toEither
  }

  def rate(a: Account) = a match {
    case SavingsAccount(_, _, r, _, _, _) => r.some
    case _ => None
  }
}


