package frdomain.ch6
package domain
package service

import java.util.Date
import squants.market._
import model.account.{AccountNo, AccountName}

sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType

abstract class AccountService[M[_], Account, Amount, Balance] {

  def open(no: AccountNo, name: AccountName, rate: Option[BigDecimal], openingDate: Option[Date], 
    accountType: AccountType): M[Account]

  def close(no: AccountNo, closeDate: Option[Date]): M[Account]

  def debit(no: AccountNo, amount: Money): M[Account]

  def credit(no: AccountNo, amount: Money): M[Account]

  def balance(no: AccountNo): M[Balance]

  def transfer(from: AccountNo, to: AccountNo, amount: Money): M[(Account, Account)] 
}

