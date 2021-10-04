package frdomain.ch6
package domain
package model

import java.time.LocalDateTime

import cats._
import cats.data._
import cats.syntax.all._

import common._

case class Balance(amount: Amount = 0)

sealed trait Account {
  def no: String
  def name: String
  def dateOfOpen: Option[LocalDateTime]
  def dateOfClose: Option[LocalDateTime]
  def balance: Balance
}

final case class CheckingAccount (no: String, name: String,
  dateOfOpen: Option[LocalDateTime], dateOfClose: Option[LocalDateTime] = None, balance: Balance = Balance()) extends Account

final case class SavingsAccount (no: String, name: String, rateOfInterest: Amount, 
  dateOfOpen: Option[LocalDateTime], dateOfClose: Option[LocalDateTime] = None, balance: Balance = Balance()) extends Account

object Account {

  private def validateAccountNo(no: String): ValidationResult[String] = 
    if (no.isEmpty || no.size < 5) s"Account No has to be at least 5 characters long: found $no".invalidNec
    else no.validNec

  private def validateOpenCloseDate(od: LocalDateTime, cd: Option[LocalDateTime]): ValidationResult[(Option[LocalDateTime], Option[LocalDateTime])] = cd.map { c => 
    if (c isBefore od) s"Close date [$c] cannot be earlier than open date [$od]".invalidNec
    else (od.some, cd).validNec
  }.getOrElse { (od.some, cd).validNec }

  private def validateRate(rate: BigDecimal): ValidationResult[BigDecimal] =
    if (rate <= BigDecimal(0)) s"Interest rate $rate must be > 0".invalidNec
    else rate.validNec

  def checkingAccount(no: String, name: String, openDate: Option[LocalDateTime], closeDate: Option[LocalDateTime], 
    balance: Balance): ErrorOr[Account] = { 

    ( 
      validateAccountNo(no),
      validateOpenCloseDate(openDate.getOrElse(today), closeDate)

    ).mapN {(n, d) =>
      CheckingAccount(n, name, d._1, d._2, balance)
    }.toEither
  }

  def savingsAccount(no: String, name: String, rate: BigDecimal, openDate: Option[LocalDateTime], 
    closeDate: Option[LocalDateTime], balance: Balance): ErrorOr[Account] = { 

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

  private def validateCloseDate(a: Account, cd: LocalDateTime): ValidationResult[LocalDateTime] = {
    if (cd isBefore a.dateOfOpen.get) s"Close date [$cd] cannot be earlier than open date [${a.dateOfOpen.get}]".invalidNec
    else cd.validNec
  }

  def close(a: Account, closeDate: LocalDateTime): ErrorOr[Account] = {
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


