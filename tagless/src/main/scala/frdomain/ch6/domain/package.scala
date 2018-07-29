package frdomain.ch6
package domain

import cats._
import cats.data._
import cats.instances.all._

import repository.AccountRepository

package object service {
  type Valid[M[_], A] = EitherT[M, AccountServiceException, A]

  // Any AccountService returns this type
  //
  // 1. It takes an AccountRepository
  // 2. It returns an Either where
  //   a. the Left indicates a service level exception
  //   b. the Right indicates the returned value
  // 3. The type is parameterized on the type constructor M[_] which is typically a Monad
  type AccountOperation[M[_], A] = Kleisli[Valid[M, ?], AccountRepository[M], A]
}
