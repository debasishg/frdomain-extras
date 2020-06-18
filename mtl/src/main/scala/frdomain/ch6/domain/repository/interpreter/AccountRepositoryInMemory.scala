package frdomain.ch6
package domain
package repository
package interpreter

import java.util.Date
import scala.collection.immutable.Map 

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._
import cats.effect.concurrent.Ref

import common._
import model.account._

class AccountRepositoryInMemory[M[+_]](repo: Ref[M, Map[AccountNo, Account]])(implicit me: MonadError[M, AppException]) extends AccountRepository[M] {

  def query(no: AccountNo): M[Option[Account]] = repo.get.map(_.get(no))  

  def store(a: Account): M[Account] = repo.update(_ + ((a.no, a))).map(_ => a)

  def query(openedOn: Date): M[List[Account]] = 
    repo.get.map(_.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toList)

  def all: M[List[Account]] = repo.get.map(_.values.toList)

  def balance(no: AccountNo): M[Option[Balance]] = 
    query(no).flatMap { 
      case Some(a) => a.balance.some.pure[M]
      case None => me.raiseError[Option[Balance]](NonExistingAccount(no))
    }
}
