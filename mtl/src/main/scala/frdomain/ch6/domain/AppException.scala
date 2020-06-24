package frdomain.ch6
package domain

import cats.data.NonEmptyChain

trait AppException {
  def message: NonEmptyChain[String]
}


