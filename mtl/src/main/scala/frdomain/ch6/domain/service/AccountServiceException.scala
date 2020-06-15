package frdomain.ch6
package domain
package service

import cats._
import cats.data._
import cats.instances.all._
import model.account.AccountNo

trait AccountServiceException extends AppException

case class AlreadyExistingAccount(no: AccountNo) extends AccountServiceException {
  val message = NonEmptyList.of(s"Already existing account with no $no")
}

case class NonExistingAccount(no: AccountNo) extends AccountServiceException {
  val message = NonEmptyList.of(s"No existing account with no $no")
}

case class ClosedAccount(no: AccountNo) extends AccountServiceException {
  val message = NonEmptyList.of(s"Account with no $no is closed")
}

case object RateMissingForSavingsAccount extends AccountServiceException {
  val message = NonEmptyList.of("Rate needs to be given for savings account")
}

case class MiscellaneousDomainExceptions(msgs: NonEmptyList[String]) extends AccountServiceException {
  val message = msgs
}


