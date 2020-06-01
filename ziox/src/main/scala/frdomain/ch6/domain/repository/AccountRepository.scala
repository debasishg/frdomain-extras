package frdomain.ch6.domain
package repository

import java.util.Date
import zio._

import common._
import model.{ Account, Balance }

object AccountRepository extends Serializable {
  trait Service extends Serializable {
    def query(no: String): Task[Option[Account]]
    def store(a: Account): Task[Account]
    def query(openedOn: Date): Task[Seq[Account]]
    def all: Task[Seq[Account]]
    def balance(no: String): Task[Option[Balance]]
  }
}