package frdomain.ch6
package domain
package service

import cats._
import cats.data._

trait TaxCalculation[Amount] {
  def computeTax: Kleisli[Valid, Amount, Amount]
}
