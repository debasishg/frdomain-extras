package frdomain.ch6
package domain
package service

import java.util.Date

import cats._
import cats.data._
import cats.instances.all._
import cats.effect.IO

import repository.AccountRepository


sealed trait AccountType
case object Checking extends AccountType
case object Savings extends AccountType

trait AccountService[M[_], Account, Amount, Balance] {

  def open(no: String, name: String, rate: Option[BigDecimal], openingDate: Option[Date], 
    accountType: AccountType): Kleisli[M, AccountRepository[M], Account]

  def close(no: String, closeDate: Option[Date]): Kleisli[M, AccountRepository[M], Account]

  def debit(no: String, amount: Amount): Kleisli[M, AccountRepository[M], Account]

  def credit(no: String, amount: Amount): Kleisli[M, AccountRepository[M], Account]

  def balance(no: String): Kleisli[M, AccountRepository[M], Balance]

  def transfer(from: String, to: String, amount: Amount): Kleisli[M, AccountRepository[M], (Account, Account)]
}
