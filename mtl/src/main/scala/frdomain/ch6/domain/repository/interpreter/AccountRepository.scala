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

class AccountRepositoryInMemory[M[+_]](implicit me: MonadError[M, AppException]) extends AccountRepository[M] {
  lazy val repo = MMap.empty[String, Account]

  def query(no: String): M[Option[Account]] = repo.get(no).pure[M]

  def store(a: Account): M[Account] = {
    val _ = repo += ((a.no, a))
    a.pure[M]
  }

  def query(openedOn: Date): M[Seq[Account]] = {
    repo.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toSeq.pure[M]
  }

  def all: M[Seq[Account]] = repo.values.toSeq.pure[M]

  def balance(no: String): M[Balance] = 
    repo.get(no).map(_.balance) match {
      case Some(b) => b.pure[M]
      case _ => me.raiseError[Balance](NonExistingAccount(no))
    }
}
