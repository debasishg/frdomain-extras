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

class AccountRepositorySkunk[M[+_]: Sync](
  // sessionPool: Resource[M, Session[M]])(implicit me: MonadError[M, AppException]) extends AccountRepository[M] {
  sessionPool: Resource[M, Session[M]]) extends AccountRepository[M] {
  import AccountQueries._

  def query(no: AccountNo): M[Option[Account]] = 
    sessionPool.use { session =>
      session.prepare(selectByAccountNo).use { ps =>
        ps.option(no)
      }
    }

  def store(a: Account): M[Account] = ???

  def query(openedOn: Date): M[List[Account]] = 
    sessionPool.use { session =>
      session.prepare(selectByOpenedDate).use { ps =>
        ps.stream(toLocalDateTime(openedOn), 1024).compile.toList
      }
    }

  def all: M[List[Account]] = sessionPool.use(_.execute(selectAll))

  def balance(no: AccountNo): M[Balance] = ??? 
}

private object AccountQueries {

  def toJavaDate(dt: LocalDateTime): Option[Date] = {
    if (dt == null) None
    else {
      val zdt = dt.atZone(ZoneId.systemDefault())
      Some(Date.from(zdt.toInstant()))
    }
  }

  def toLocalDateTime(dt: Date): LocalDateTime = {
    LocalDateTime.ofInstant(dt.toInstant(), ZoneId.systemDefault())
  }

  val decoder: Decoder[Account] =
    (varchar ~ varchar ~ varchar ~ numeric ~ timestamp ~ timestamp ~ numeric).map {
      case no ~ nm ~ tp ~ ri ~ dp ~ dc ~ bl =>
        if (tp == "Checking") {
          CheckingAccount(AccountNo(no), AccountName(nm), toJavaDate(dp), toJavaDate(dc), Balance(USD(bl)))
        } else {
          SavingsAccount(AccountNo(no), AccountName(nm), ri, toJavaDate(dp), toJavaDate(dc), Balance(USD(bl)))
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
}