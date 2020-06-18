package frdomain.ch6
package domain
package repository
package interpreter

import java.time.LocalDateTime
import scala.collection.immutable.Map 

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._
import cats.effect.concurrent.Ref
import cats.effect.Sync

import common._
import model.account._

// Constructor private for the interpreter to prevent the Ref from leaking
// access through smart constructor below
final class AccountRepositoryInMemory[M[_]: Monad] private (repo: Ref[M, Map[AccountNo, Account]])
  extends AccountRepository[M] {

  def query(no: AccountNo): M[Option[Account]] = repo.get.map(_.get(no))  

  def store(a: Account): M[Account] = repo.update(_ + ((a.no, a))).map(_ => a)

  def query(openedOn: LocalDateTime): M[List[Account]] = 
    repo.get.map(_.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toList)

  def all: M[List[Account]] = repo.get.map(_.values.toList)

  def balance(no: AccountNo): M[Option[Balance]] = query(no).map(_.map(_.balance))
}

// Smart constructor 
object AccountRepositoryInMemory {
  def make[M[+_]: Sync]: M[AccountRepositoryInMemory[M]] =
    Ref.of[M, Map[AccountNo, Account]](Map.empty).map(new AccountRepositoryInMemory(_))
}
