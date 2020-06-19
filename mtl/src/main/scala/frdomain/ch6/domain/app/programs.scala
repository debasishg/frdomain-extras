package frdomain.ch6.domain
package io
package app

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.mtl._

import squants.market._

import repository._
import service._
import service.interpreter._
import model.account._
import common._

import Implicits._

object programs {
  def programNormalOps[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
    (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService._
    import reportingService._

    val opens = 
      for {
        _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
        _ <- open(AccountNo("a2345"), AccountName("a2name"), None, None, Checking)
        _ <- open(AccountNo("a3456"), AccountName("a3name"), BigDecimal(5.8).some, None, Savings)
        _ <- open(AccountNo("a4567"), AccountName("a4name"), None, None, Checking)
        _ <- open(AccountNo("a5678"), AccountName("a5name"), BigDecimal(2.3).some, None, Savings)
      } yield (())
  
    val credits = 
      for {
        _ <- credit(AccountNo("a1234"), USD(1000))
        _ <- credit(AccountNo("a2345"), USD(2000))
        _ <- credit(AccountNo("a3456"), USD(3000))
        _ <- credit(AccountNo("a4567"), USD(4000))
      } yield (())
  
    for {
      _ <- opens
      _ <- credits
      a <- balanceByAccount
    } yield a
  }

  def programCreditNonExistingAccount[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
    (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService._
    import reportingService._

    for {
      _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
      _ <- credit(AccountNo("a2345"), USD(2000))
      a <- balanceByAccount
    } yield a
  }

  def programInsufficientAmountToDebit[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
    (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService._
    import reportingService._

    for {
      _ <- open(AccountNo("a1234"), AccountName("a1name"), None, None, Checking)
      _ <- credit(AccountNo("a1234"), USD(2000))
      _ <- debit(AccountNo("a1234"), USD(4000))
      a <- balanceByAccount
    } yield a
  }

  def programMultipleFails[F[_]](accountService: AccountService[F, Account, Amount, Balance], reportingService: ReportingService[F, Amount])
    (implicit me : MonadError[F, AppException]): F[Seq[(AccountNo, Money)]] = {

    import accountService._
    import reportingService._

    for {
      a <- open(AccountNo("a134"), AccountName("a1name"), Some(BigDecimal(-0.9)), None, Savings)
      _ <- credit(a.no, USD(2000))
      _ <- debit(a.no, USD(4000))
      b <- balanceByAccount
    } yield b
  }
}
