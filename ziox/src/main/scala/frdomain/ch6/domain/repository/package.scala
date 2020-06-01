package frdomain.ch6.domain

import zio._
import model.{ Account, Balance }
import java.util.Date

package object repository {
  type AccountRepository = Has[AccountRepository.Service]

  def query(no: String): RIO[AccountRepository, Option[Account]] =
    ZIO.accessM(_.get.query(no))

  def all: RIO[AccountRepository, Seq[Account]] =
    ZIO.accessM(_.get.all)

  def store(a: Account): RIO[AccountRepository, Account] =
    ZIO.accessM(_.get.store(a))

  def query(openedOn: Date): RIO[AccountRepository, Seq[Account]] =
    ZIO.accessM(_.get.query(openedOn))

  def balance(no: String): RIO[AccountRepository, Option[Balance]] =
    ZIO.accessM(_.get.balance(no))
}
