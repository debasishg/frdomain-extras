package frdomain.ch6.domain
package repository

import java.util.Date
import zio._

import model.{ Account, Balance }
import common._

class InMemoryAccountRepository(ref: Ref[Map[String, Account]])  {
  val accountRepository = new AccountRepository.Service {

    override def all: Task[Seq[Account]] =
      ref.get.map(_.values.toList)

    override def query(no: String): Task[Option[Account]] =
      ref.get.map(_.get(no))

    override def query(openedOn: Date): Task[Seq[Account]] =
      ref.get.map(_.values.filter(_.dateOfOpen.getOrElse(today) == openedOn).toSeq)

    override def store(a: Account): Task[Account] =
      ref.update(m => m + (a.no -> a)).map(_ => a)

    override def balance(no: String): Task[Option[Balance]] =
      ref.get.map(_.get(no).map(_.balance)) 
  }
}

object InMemoryAccountRepository {

  val layer: ZLayer[Any, Throwable, AccountRepository] =
    ZLayer.fromEffect {
      for {
        ref <- Ref.make(Map.empty[String, Account])
      } yield new InMemoryAccountRepository(ref).accountRepository
    }
}