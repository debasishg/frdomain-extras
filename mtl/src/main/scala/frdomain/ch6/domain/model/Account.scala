package frdomain.ch6
package domain
package model

import java.time.LocalDateTime

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.{ Uuid, ValidBigDecimal }
import eu.timepit.refined.types.string.NonEmptyString
import _root_.io.estatico.newtype.macros.newtype
import squants.market._

import common._

object account {

  @newtype case class AccountNo(value: String)
  @newtype case class AccountName(value: String)
  
  final val ZERO_USD = USD(BigDecimal(0.0))
  case class Balance(amount: Money = ZERO_USD)
  final val ZERO_BALANCE = Balance()

  sealed trait Account {
    def no: AccountNo
    def name: AccountName
    def dateOfOpen: Option[LocalDateTime]
    def dateOfClose: Option[LocalDateTime]
    def balance: Balance
  }
  
  final case class CheckingAccount (no: AccountNo, name: AccountName,
    dateOfOpen: Option[LocalDateTime], dateOfClose: Option[LocalDateTime] = None, balance: Balance = ZERO_BALANCE) extends Account
  
  final case class SavingsAccount (no: AccountNo, name: AccountName, rateOfInterest: Amount, 
    dateOfOpen: Option[LocalDateTime], dateOfClose: Option[LocalDateTime] = None, balance: Balance = ZERO_BALANCE) extends Account
  
  object Account {
  
    private def validateAccountNo(no: AccountNo): ValidationResult[AccountNo] = 
      if (no.value.isEmpty || no.value.size < 5) s"Account No has to be at least 5 characters long: found $no".invalidNel
      else no.validNel
  
    private def validateOpenCloseDate(od: LocalDateTime, 
      cd: Option[LocalDateTime]): ValidationResult[(Option[LocalDateTime], Option[LocalDateTime])] = cd.map { c => 

      if (c isBefore od) s"Close date [$c] cannot be earlier than open date [$od]".invalidNel
      else (od.some, cd).validNel
    }.getOrElse { (od.some, cd).validNel }
  
    private def validateRate(rate: BigDecimal): ValidationResult[BigDecimal] =
      if (rate <= BigDecimal(0)) s"Interest rate $rate must be > 0".invalidNel
      else rate.validNel
  
    def checkingAccount(no: AccountNo, name: AccountName, openDate: Option[LocalDateTime], closeDate: Option[LocalDateTime], 
      balance: Balance): ErrorOr[Account] = { 
  
      ( 
        validateAccountNo(no),
        validateOpenCloseDate(openDate.getOrElse(today), closeDate)
  
      ).mapN {(n, d) =>
        CheckingAccount(n, name, d._1, d._2, balance)
      }.toEither
    }
  
    def savingsAccount(no: AccountNo, name: AccountName, rate: BigDecimal, openDate: Option[LocalDateTime], 
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
      if (a.dateOfClose isDefined) s"Account ${a.no} is already closed".invalidNel
      else a.validNel
    }
  
    private def validateCloseDate(a: Account, cd: LocalDateTime): ValidationResult[LocalDateTime] = {
      if (cd isBefore a.dateOfOpen.get) s"Close date [$cd] cannot be earlier than open date [${a.dateOfOpen.get}]".invalidNel
      else cd.validNel
    }
  
    def close(a: Account, closeDate: LocalDateTime): ErrorOr[Account] = {
      (validateAccountAlreadyClosed(a), validateCloseDate(a, closeDate)).mapN {(acc, _) =>
        acc match {
          case c: CheckingAccount => c.copy(dateOfClose = Some(closeDate))
          case s: SavingsAccount  => s.copy(dateOfClose = Some(closeDate))
        }
      }.toEither
    }
  
    private def checkBalance(a: Account, amount: Money): ValidationResult[Account] = {
      if (amount < ZERO_USD && a.balance.amount < (-1) * amount) s"Insufficient amount in ${a.no} to debit".invalidNel
      else a.validNel
    }
  
    def updateBalance(a: Account, amount: Money): ErrorOr[Account] = {
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
}