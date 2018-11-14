package frdomain.ch6
package domain
package service

import cats._
import cats.data._
import cats.instances.all._


trait InterestCalculation[M[_], Account, Amount] {
  def computeInterest: Kleisli[M, Account, Amount]
}
