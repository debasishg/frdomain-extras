package frdomain.ch6
package domain
package service

import zio._
import cats.data.NonEmptyList

import common._
import repository.AccountRepository
import model.Account

object ReportingService {
  trait Service {
    def balanceByAccount: IO[ReportingServiceException, Seq[(String, Amount)]]
  }

  val live = ZLayer.fromService[AccountRepository.Service, ReportingService.Service] {
    (repo: AccountRepository.Service) =>

    new Service {
      def balanceByAccount: IO[ReportingServiceException, Seq[(String, Amount)]] =
        withReportingServiceException(repo.all.map { accounts => accounts.map(a => (a.no, a.balance.amount)) })
    }
  }

  private def withReportingServiceException[A](t: Task[A]): IO[ReportingServiceException, A] = 
    t.foldM(
      error   => IO.fail(ReportingServiceException(NonEmptyList.of(error.getMessage))),
      success => IO.succeed(success)
    )
}
