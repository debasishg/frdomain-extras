package frdomain.ch6.domain
package repository

import java.util.Date

import zio._

import common._

import model.{ Account, Balance }

object AccountRepository extends Serializable {
  trait Service extends Serializable {
    def query(no: String): UIO[Option[Account]]
    def store(a: Account): UIO[Account]
    def query(openedOn: Date): UIO[Seq[Account]]
    def all: UIO[Seq[Account]]
    def balance(no: String): UIO[Balance]
  }
}