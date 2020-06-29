package frdomain.ch6
package domain
package repository

import java.time.LocalDateTime

import cats._
import cats.data._
import cats.instances.all._

import common._

import model.{ Account, Balance }

trait AccountRepository[M[_]] { 
  def query(no: String): M[Option[Account]]
  def store(a: Account): M[Account]
  def query(openedOn: LocalDateTime): M[Seq[Account]]
  def all: M[Seq[Account]]
  def balance(no: String): M[Balance] 
}
