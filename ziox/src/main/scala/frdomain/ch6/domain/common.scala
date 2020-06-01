package frdomain.ch6
package domain

import java.util.{ Date, Calendar }
import cats.data._

object common {
  type Amount = BigDecimal
  type ValidationResult[A] = ValidatedNel[String, A]
  type ErrorOr[A] = Either[NonEmptyList[String], A]

  def today = Calendar.getInstance.getTime
}

