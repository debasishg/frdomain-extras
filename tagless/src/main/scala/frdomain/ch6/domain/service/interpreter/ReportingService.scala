package frdomain.ch6
package domain
package service
package interpreter

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import repository.AccountRepository
import common._


class ReportingServiceInterpreter[M[_]](implicit me: MonadError[M, Throwable]) extends ReportingService[M, Amount] {

  def balanceByAccount: ReportOperation[Seq[(String, Amount)]] = Kleisli[M, AccountRepository[M], Seq[(String, Amount)]] { (repo: AccountRepository[M]) =>
  repo.all.map { accounts => accounts.map(a => (a.no, a.balance.amount)) }
  }
} 
