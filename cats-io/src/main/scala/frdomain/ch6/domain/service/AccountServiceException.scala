package frdomain.ch6
package domain
package service

import cats._
import cats.data._

trait AccountServiceException {
  def message: NonEmptyChain[String]
}

case class AlreadyExistingAccount(no: String) extends AccountServiceException {
  val message = NonEmptyChain(s"Already existing account with no $no")
}

case class NonExistingAccount(no: String) extends AccountServiceException {
  val message = NonEmptyChain(s"No existing account with no $no")
}

case class ClosedAccount(no: String) extends AccountServiceException {
  val message = NonEmptyChain(s"Account with no $no is closed")
}

case object RateMissingForSavingsAccount extends AccountServiceException {
  val message = NonEmptyChain("Rate needs to be given for savings account")
}

case class MiscellaneousDomainExceptions(msgs: NonEmptyChain[String]) extends AccountServiceException {
  val message = msgs
}

