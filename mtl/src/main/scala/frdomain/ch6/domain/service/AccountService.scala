package frdomain.ch6
package domain
package service

import java.util.Date

sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType

abstract class AccountService[M[_], Account, Amount, Balance] {

  def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
    accountType: AccountType): M[Account]

  def close(no: String, closeDate: Option[Date]): M[Account]

  def debit(no: String, amount: Amount): M[Account]

  def credit(no: String, amount: Amount): M[Account]

  def balance(no: String): M[Balance]

  def transfer(from: String, to: String, amount: Amount): M[(Account, Account)] 
}

