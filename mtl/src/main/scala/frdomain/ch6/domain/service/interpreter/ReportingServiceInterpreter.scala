package frdomain.ch6
package domain
package service
package interpreter

import cats._
import cats.implicits._
import cats.mtl._
import squants.market._

import repository.AccountRepository
import common._
import model.account.AccountNo

class ReportingServiceInterpreter[M[+_]]
  (implicit E: MonadError[M, AppException], A: ApplicativeAsk[M, AccountRepository[M]]) 
    extends ReportingService[M, Amount] {

  def balanceByAccount: M[Seq[(AccountNo, Money)]] = for {

    repo        <- A.ask
    allBalances <- repo.all
    balances    <- allBalances.map(account => (account.no, account.balance.amount)).pure[M]

  } yield balances
} 

