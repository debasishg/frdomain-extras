package frdomain.ch6
package domain
package monixtask
package app

import cats._
import cats.data._
import cats.syntax.all._
import cats.instances.all._

import service.AccountServiceException
import service.interpreter.{ AccountServiceInterpreter, InterestPostingServiceInterpreter, ReportingServiceInterpreter }
import repository.interpreter.AccountRepositoryInMemory
import service.{ Checking, Savings }
import common._
import model.Account

object App {

  import monix.eval.Task
  import monix.execution.Callback
  import monix.execution.Scheduler.Implicits.global
  import monix.execution.CancelableFuture

  import scala.concurrent.Await
  import scala.concurrent.duration.Duration

  import scala.util.{ Success, Failure }

  val accountServiceTask = new AccountServiceInterpreter[Task]
  val interestPostingServiceTask = new InterestPostingServiceInterpreter[Task]
  val reportingServiceTask = new ReportingServiceInterpreter[Task]

  import accountServiceTask._
  import interestPostingServiceTask._
  import reportingServiceTask._

  def main(args: Array[String]) = {
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
  
    // task is lazy: Task allows us to define the task
    // execution of the ask can be handled separately
    val task = c(new AccountRepositoryInMemory[Task])

    // strategy of execution #1 
    // run the task asynchronously via a callback
    val _ = task.runAsync {
      case Left(th) => th.printStackTrace
      case Right(value) => value.foreach(println)
    }

    // strategy of execution #2 
    // convert the task to a future and then run the future
    val future: CancelableFuture[Seq[(String, Amount)]] = task.runToFuture
    future.foreach(println)

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
    val _ = task.runAsync {
      case Left(th) => th.printStackTrace
      case Right(value) => value.foreach(println)
    }

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
    val _ = task.runAsync {
      case Left(th) => th.printStackTrace
      case Right(value) => value.foreach(println)
    }

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
    val _ = task.runAsync {
      case Left(th) => th.printStackTrace
      case Right(value) => value.foreach(println)
    }

    // NonEmptyList(Account No has to be at least 5 characters long: found a134, Interest rate -0.9 must be > 0)
  }
}

