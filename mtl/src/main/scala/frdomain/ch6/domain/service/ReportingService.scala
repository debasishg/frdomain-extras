package frdomain.ch6
package domain
package service

import model.account.AccountNo

trait ReportingService[M[_], Amount] {
  def balanceByAccount: M[Seq[(AccountNo, Amount)]]
} 

