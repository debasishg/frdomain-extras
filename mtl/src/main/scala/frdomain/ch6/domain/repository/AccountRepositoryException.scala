package frdomain.ch6
package domain
package repository

import cats._
import cats.data._
import cats.instances.all._

trait AccountRepositoryException extends AppException

case class NonExistingAccount(no: String) extends AccountRepositoryException {
  val message = NonEmptyList.of(s"No existing account with no $no")
}
