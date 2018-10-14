package frdomain.ch6
package domain

import cats.data.NonEmptyList

trait AppException {
  def message: NonEmptyList[String]
}


