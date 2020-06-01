package frdomain.ch6.domain

import java.util.Date
import zio._

import model.{ Account, Balance }
import common._

package object service {
  type AccountService = Has[AccountService.Service]
  
  def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
    accountType: AccountType): ZIO[AccountService, AccountServiceException, Account] = 
    ZIO.accessM(_.get.open(no, name, rate, openingDate, accountType))

  def close(no: String, closeDate: Option[Date]): ZIO[AccountService, AccountServiceException, Account] = 
    ZIO.accessM(_.get.close(no, closeDate))

  def debit(no: String, amount: Amount): ZIO[AccountService, AccountServiceException, Account] =
    ZIO.accessM(_.get.debit(no, amount))

  def credit(no: String, amount: Amount): ZIO[AccountService, AccountServiceException, Account] =
    ZIO.accessM(_.get.credit(no, amount))

  def balance(no: String): ZIO[AccountService, AccountServiceException, Balance] = 
    ZIO.accessM(_.get.balance(no))

  def transfer(from: String, to: String, amount: Amount): ZIO[AccountService, AccountServiceException, (Account, Account)] =
    ZIO.accessM(_.get.transfer(from, to, amount))
}
