package frdomain.ch6
package domain
package repository
package interpreter

import java.util.Date
import java.time.{ LocalDateTime, ZoneId, LocalDate }

import cats.implicits._
import cats.effect._
import cats.effect.concurrent.Ref

import skunk._
import skunk.codec.all._
import skunk.implicits._

import squants.market._
import common._
import model.account._
import ext.skunkx._

final class AccountRepositorySkunk[M[+_]: Sync](
  sessionPool: Resource[M, Session[M]]) extends AccountRepository[M] {
  import AccountQueries._

  def query(no: AccountNo): M[Option[Account]] = 
    sessionPool.use { session =>
      session.prepare(selectByAccountNo).use { ps =>
        ps.option(no)
      }
    }

  def store(a: Account): M[Account] = 
    sessionPool.use { session =>
      session.prepare(upsertAccount).use { cmd =>
        cmd.execute(a).void.map(_ => a)
      }
    }

  def query(openedOn: LocalDateTime): M[List[Account]] = 
    sessionPool.use { session =>
      session.prepare(selectByOpenedDate).use { ps =>
        ps.stream(openedOn, 1024).compile.toList
      }
    }

  def all: M[List[Account]] = sessionPool.use(_.execute(selectAll))

  def balance(no: AccountNo): M[Option[Balance]] = query(no).map(_.map(_.balance))
}

private object AccountQueries {

  val decoder: Decoder[Account] =
    (varchar ~ varchar ~ varchar ~ numeric ~ timestamp ~ timestamp ~ numeric).map {
      case no ~ nm ~ tp ~ ri ~ dp ~ dc ~ bl =>
        if (tp == "Checking") {
          CheckingAccount(AccountNo(no), AccountName(nm), Option(dp), Option(dc), Balance(USD(bl)))
        } else {
          SavingsAccount(AccountNo(no), AccountName(nm), ri, Option(dp), Option(dc), Balance(USD(bl)))
        }
    }

  val selectByAccountNo: Query[AccountNo, Account] =
    sql"""
        SELECT a.no, a.name, a.type, a.rateOfInterest, a.dateOfOpen, a.dateOfClose, a.balance
        FROM accounts AS a
        WHERE a.no = ${varchar.cimap[AccountNo]}
       """.query(decoder)

  val selectByOpenedDate: Query[LocalDateTime, Account] =
    sql"""
        SELECT a.no, a.name, a.type, a.rateOfInterest, a.dateOfOpen, a.dateOfClose, a.balance
        FROM accounts AS a
        WHERE a.dateOfOpen = $timestamp
       """.query(decoder)

  val selectAll: Query[Void, Account] =
    sql"""
        SELECT a.no, a.name, a.type, a.rateOfInterest, a.dateOfOpen, a.dateOfClose, a.balance
        FROM accounts
       """.query(decoder)

  val insertAccount: Command[Account] =
    sql"""
        INSERT INTO accounts
        VALUES ($varchar, $varchar, $varchar, $numeric, $timestamp, $timestamp, $numeric)
       """.command.contramap {

      case a => a match {
        case CheckingAccount(no, nm, dop, doc, b) =>
          no.value ~ nm.value ~ "Checking" ~ BigDecimal(0) ~ dop.getOrElse(null) ~ doc.getOrElse(null) ~ b.amount.amount
        case SavingsAccount(no, nm, r, dop, doc, b) =>
          no.value ~ nm.value ~ "Savings" ~ r ~ dop.getOrElse(null) ~ doc.getOrElse(null) ~ b.amount.amount
      }
    }

  val updateAccount: Command[Account] =
    sql"""
        UPDATE accounts SET
          name             = $varchar,
          type             = $varchar,
          rateOfInterest   = $numeric,
          dateOfOpen       = $timestamp,
          dateOfClose      = $timestamp,
          balance          = $numeric
        WHERE no = $varchar
       """.command.contramap {

      case a => a match {
        case CheckingAccount(no, nm, dop, doc, b) =>
          nm.value ~ "Checking" ~ BigDecimal(0) ~ dop.getOrElse(null) ~ doc.getOrElse(null) ~ b.amount.amount ~ no.value
        case SavingsAccount(no, nm, r, dop, doc, b) =>
          nm.value ~ "Savings" ~ r ~ dop.getOrElse(null) ~ doc.getOrElse(null) ~ b.amount.amount ~ no.value
      }
    }

  val upsertAccount: Command[Account] =
    sql"""
        INSERT INTO accounts
        VALUES ($varchar, $varchar, $varchar, $numeric, $timestamp, $timestamp, $numeric)
        ON CONFLICT(no) DO UPDATE SET
          name             = EXCLUDED.name,
          type             = EXCLUDED.type,
          rateOfInterest   = EXCLUDED.rateOfInterest,
          dateOfOpen       = EXCLUDED.dateOfOpen,
          dateOfClose      = EXCLUDED.dateOfClose,
          balance          = EXCLUDED.balance
       """.command.contramap {

      case a => a match {
        case CheckingAccount(no, nm, dop, doc, b) =>
          no.value ~ nm.value ~ "Checking" ~ BigDecimal(0) ~ dop.getOrElse(null) ~ doc.getOrElse(null) ~ b.amount.amount
        case SavingsAccount(no, nm, r, dop, doc, b) =>
          no.value ~ nm.value ~ "Savings" ~ r ~ dop.getOrElse(null) ~ doc.getOrElse(null) ~ b.amount.amount
      }
    }
}

// Smart constructor 
object AccountRepositorySkunk {
  def make[M[+_]: Sync](
    sessionPool: Resource[M, Session[M]]
  ): M[AccountRepositorySkunk[M]] = Sync[M].delay(new AccountRepositorySkunk[M](sessionPool))
}