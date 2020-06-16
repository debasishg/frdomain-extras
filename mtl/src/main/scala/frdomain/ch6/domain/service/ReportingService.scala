package frdomain.ch6
package domain
package service

import squants.market._
import model.account.AccountNo

trait ReportingService[M[_], Amount] {
  def balanceByAccount: M[Seq[(AccountNo, Money)]]
} 

