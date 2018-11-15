package frdomain.ch6
package domain
package monixtask
package app

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import service.AccountServiceException
import service.interpreter.{ AccountServiceInterpreter, InterestPostingServiceInterpreter, ReportingServiceInterpreter }
import repository.interpreter.AccountRepositoryInMemory
import service.{ Checking, Savings }
import common._
import model.Account

object App {

  import monix.eval.Task
  import monix.eval.Callback
  import monix.execution.Scheduler.Implicits.global

  import scala.concurrent.Await
  import scala.concurrent.duration.Duration

  import scala.util.{ Success, Failure }
 

  val accountServiceTask = new AccountServiceInterpreter[Task]
  val interestPostingServiceTask = new InterestPostingServiceInterpreter[Task]
  val reportingServiceTask = new ReportingServiceInterpreter[Task]

  import accountServiceTask._
  import interestPostingServiceTask._
  import reportingServiceTask._

  def main(args: Array[String]): Unit = {
    usecase1()
    usecase2()
    usecase3()
    usecase4()
  }

  def usecase1(): Unit = {
    val opens = 
      for {
        _ <- open("a1234", "a1name", None, None, Checking)
        _ <- open("a2345", "a2name", None, None, Checking)
        _ <- open("a3456", "a3name", BigDecimal(5.8).some, None, Savings)
        _ <- open("a4567", "a4name", None, None, Checking)
        _ <- open("a5678", "a5name", BigDecimal(2.3).some, None, Savings)
      } yield (())
  
    val credits = 
      for {
        _ <- credit("a1234", 1000)
        _ <- credit("a2345", 2000)
        _ <- credit("a3456", 3000)
        _ <- credit("a4567", 4000)
      } yield (())
  
    val c = for {
      _ <- opens
      _ <- credits
      a <- balanceByAccount
    } yield a
  
    val task = c(new AccountRepositoryInMemory[Task])


    val _ = task.runAsync(new Callback[Seq[(String, Amount)]] {
      def onSuccess(value: Seq[(String, Amount)]): Unit = value.foreach(println)
      def onError(ex: Throwable): Unit = ex.printStackTrace
    })

    // (a2345,2000)
    // (a5678,0)
    // (a3456,3000)
    // (a1234,1000)
    // (a4567,4000)
  }

  def usecase2(): Unit = {
    val c = for {
      _ <- open("a1234", "a1name", None, None, Checking)
      _ <- credit("a2345", 2000)
      a <- balanceByAccount
    } yield a

    val task = c(new AccountRepositoryInMemory[Task])

    val _ = task.runAsync(new Callback[Seq[(String, Amount)]] {
      def onSuccess(value: Seq[(String, Amount)]): Unit = value.foreach(println)
      def onError(ex: Throwable): Unit = ex.printStackTrace
    })

    // NonEmptyList(No existing account with no a2345)
  }

  def usecase3(): Unit = {
    val c = for {
      _ <- open("a1234", "a1name", None, None, Checking)
      _ <- credit("a1234", 2000)
      _ <- debit("a1234", 4000)
      a <- balanceByAccount
    } yield a

    val task = c(new AccountRepositoryInMemory[Task])

    val _ = task.runAsync(new Callback[Seq[(String, Amount)]] {
      def onSuccess(value: Seq[(String, Amount)]): Unit = value.foreach(println)
      def onError(ex: Throwable): Unit = ex.printStackTrace
    })

    // NonEmptyList(Insufficient amount in a1234 to debit)
  }

  def usecase4(): Unit = {
    val c = for {
      a <- open("a134", "a1name", Some(BigDecimal(-0.9)), None, Savings)
      _ <- credit(a.no, 2000)
      _ <- debit(a.no, 4000)
      b <- balanceByAccount
    } yield b

    val task = c(new AccountRepositoryInMemory[Task])

    val _ = task.runAsync(new Callback[Seq[(String, Amount)]] {
      def onSuccess(value: Seq[(String, Amount)]): Unit = value.foreach(println)
      def onError(ex: Throwable): Unit = ex.printStackTrace
    })

    // NonEmptyList(Account No has to be at least 5 characters long: found a134, Interest rate -0.9 must be > 0)
  }
}

