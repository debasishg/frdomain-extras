package frdomain.ch6
package domain
package service

import cats._
import cats.data._
import cats.instances.all._

import repository.AccountRepository


trait ReportingService[M[_], Amount] {
  type ReportOperation[A] = Kleisli[M, AccountRepository[M], A]

  def balanceByAccount: ReportOperation[Seq[(String, Amount)]]
} 
