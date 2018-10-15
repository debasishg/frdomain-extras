package frdomain.ch6
package domain
package service

trait ReportingService[M[_], Amount] {
  def balanceByAccount: M[Seq[(String, Amount)]]
} 

