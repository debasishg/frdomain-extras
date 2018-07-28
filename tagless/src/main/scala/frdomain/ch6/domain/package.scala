package frdomain.ch6
package domain

import cats._
import cats.data._
import cats.instances.all._
import cats.effect.IO

import repository.AccountRepository

package object service {
  type Valid[M[_], A] = EitherT[M, AccountServiceException, A]
  type AccountOperation[M[_], A] = Kleisli[Valid[M, ?], AccountRepository[M], A]
}
