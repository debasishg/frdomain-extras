package frdomain.ch6
package domain
package repository
package interpreter

import java.util.Date
import scala.collection.mutable.{ Map => MMap }

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import common._
import model.{ Account, Balance }

class AccountRepositoryInMemory[M[+_]](implicit me: MonadError[M, Throwable]) extends AccountRepository[M] {
  lazy val repo = MMap.empty[String, Account]

  def query(no: String): M[ErrorOr[Option[Account]]] = Right(repo.get(no)).pure[M]

  def store(a: Account): M[ErrorOr[Account]] = {
    val _ = repo += ((a.no, a))
    Right(a).pure[M]
  }

  def query(openedOn: Date): M[ErrorOr[Seq[Account]]] = {
    Right(repo.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toSeq).pure[M]
  }

  def all: M[ErrorOr[Seq[Account]]] = Right(repo.values.toSeq).pure[M]

  def balance(no: String): M[ErrorOr[Balance]] = query(no).map {
    case Right(Some(a)) => Right(a.balance)
    case Right(None) => Left(NonEmptyList.of(s"No account exists with no $no"))
    case Left(x) => Left(x)
  }
}
