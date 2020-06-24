package frdomain.ch6
package domain
package service

import cats._
import cats.data._
import cats.instances.all._
import model.account.AccountNo

trait AccountServiceException extends AppException

case class AlreadyExistingAccount(no: AccountNo) extends AccountServiceException {
  val message = NonEmptyChain(s"Already existing account with no $no")
}

case class NonExistingAccount(no: AccountNo) extends AccountServiceException {
  val message = NonEmptyChain(s"No existing account with no $no")
}

case class ClosedAccount(no: AccountNo) extends AccountServiceException {
  val message = NonEmptyChain(s"Account with no $no is closed")
}

case object RateMissingForSavingsAccount extends AccountServiceException {
  val message = NonEmptyChain("Rate needs to be given for savings account")
}

case class MiscellaneousDomainExceptions(msgs: NonEmptyChain[String]) extends AccountServiceException {
  val message = msgs
}


