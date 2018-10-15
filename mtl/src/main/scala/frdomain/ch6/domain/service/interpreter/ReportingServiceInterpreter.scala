package frdomain.ch6
package domain
package service
package interpreter

import cats._
import cats.implicits._
import cats.mtl._

import repository.AccountRepository
import common._

class ReportingServiceInterpreter[M[+_]]
  (implicit E: MonadError[M, AppException], A: ApplicativeAsk[M, AccountRepository[M]]) 
    extends ReportingService[M, Amount] {

  def balanceByAccount: M[Seq[(String, Amount)]] = for {

    repo        <- A.ask
    allBalances <- repo.all
    balances    <- allBalances.map(account => (account.no, account.balance.amount)).pure[M]

  } yield balances
} 

