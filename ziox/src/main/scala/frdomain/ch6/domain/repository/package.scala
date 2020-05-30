package frdomain.ch6.domain

import zio._
import model.{ Account, Balance }
import java.util.Date

package object repository {
  type AccountRepository = Has[AccountRepository.Service]

  def query(no: String): URIO[AccountRepository, Option[Account]] =
    ZIO.accessM(_.get.query(no))

  def all: URIO[AccountRepository, Seq[Account]] =
    ZIO.accessM(_.get.all)

  def store(a: Account): URIO[AccountRepository, Account] =
    ZIO.accessM(_.get.store(a))

  def query(openedOn: Date): URIO[AccountRepository, Seq[Account]] =
    ZIO.accessM(_.get.query(openedOn))

  def balance(no: String): URIO[AccountRepository, Balance] =
    ZIO.accessM(_.get.balance(no))
}
