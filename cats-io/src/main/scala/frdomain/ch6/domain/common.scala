package frdomain.ch6
package domain

import java.time.LocalDateTime
import cats._
import cats.data._

object common {
  type Amount = BigDecimal
  type ValidationResult[A] = ValidatedNec[String, A]
  type ErrorOr[A] = Either[NonEmptyChain[String], A]

  def today = LocalDateTime.now
}

