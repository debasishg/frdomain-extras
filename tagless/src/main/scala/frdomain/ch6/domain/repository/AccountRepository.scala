package frdomain.ch6
package domain
package repository

import java.util.Date

import cats._
import cats.data._
import cats.instances.all._

import common._

import model.{ Account, Balance }

trait AccountRepository[M[_]] { 
  def query(no: String): M[ErrorOr[Option[Account]]]
  def store(a: Account): M[ErrorOr[Account]]
  def query(openedOn: Date): M[ErrorOr[Seq[Account]]]
  def all: M[ErrorOr[Seq[Account]]]
  def balance(no: String): M[ErrorOr[Balance]] 
}
