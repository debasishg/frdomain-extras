package frdomain.ch6
package domain
package service

import cats._
import cats.data._

import repository.AccountRepository


trait ReportingService[Amount] {
  type ReportOperation[A] = Kleisli[Valid, AccountRepository, A]

  def balanceByAccount: ReportOperation[Seq[(String, Amount)]]
} 
