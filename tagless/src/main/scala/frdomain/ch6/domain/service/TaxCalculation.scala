package frdomain.ch6
package domain
package service

import cats._
import cats.data._
import cats.instances.all._

trait TaxCalculation[M[_], Amount] {
  def computeTax: Kleisli[Valid[M, ?], Amount, Amount]
}
