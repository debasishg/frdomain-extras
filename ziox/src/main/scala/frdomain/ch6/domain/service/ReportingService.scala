package frdomain.ch6
package domain
package service

import zio._

import common._
import repository.AccountRepository
import model.Account

object ReportingService {
  trait Service {
    def balanceByAccount: Task[Seq[(String, Amount)]]
  }

  val live = ZLayer.fromService[AccountRepository.Service, ReportingService.Service] {
    (repo: AccountRepository.Service) =>

    new Service {
      def balanceByAccount: Task[Seq[(String, Amount)]] =
        repo.all.map { accounts => accounts.map(a => (a.no, a.balance.amount)) }
    }
  }
}
