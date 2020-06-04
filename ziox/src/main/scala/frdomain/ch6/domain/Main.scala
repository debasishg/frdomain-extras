package frdomain.ch6.domain

import java.io.IOException
// import zio._

import common._
import repository.{InMemoryAccountRepository, DoobieAccountRepository}
import service._
import model.{Account, Balance}

import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

import cats._
import cats.data._
import cats.effect._
import cats.implicits._

object Main { 

  def main(args: Array[String]): Unit = {
    doobie()

    /*
    // uses AccountService
    val opens = 
      for {
        _ <- open("a1234", "a1name", None, None, Checking)
        _ <- open("a2345", "a2name", None, None, Checking)
        _ <- open("a3456", "a3name", Some(BigDecimal(5.8)), None, Savings)
        _ <- open("a4567", "a4name", None, None, Checking)
        _ <- open("a5678", "a5name", Some(BigDecimal(2.3)), None, Savings)
      } yield (())
  
    // uses AccountService
    val credits = 
      for {
        _ <- credit("a1234", 1000)
        _ <- credit("a2345", 2000)
        _ <- credit("a3456", 3000)
        _ <- credit("a4567", 4000)
      } yield (())
  
    // uses AccountService and ReportingService
    val program = for {
      _ <- opens
      _ <- credits
      a <- balanceByAccount
    } yield a
  
    // layers
    val appLayer = 
      InMemoryAccountRepository.layer >+> 
      AccountService.live >+> 
      ReportingService.live

    val banking = program.provideLayer(appLayer) 

    println(Runtime.default.unsafeRun(banking))
    // List((a5678,0), (a3456,3000), (a1234,1000), (a2345,2000), (a4567,4000))
    */

  }

  def doobie(): Unit = {
    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",     // driver classname
      "jdbc:postgresql:world",     // connect URL (driver-specific)
      "postgres",                  // user
      "",                          // password
      Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
    )

    import DoobieAccountRepository._
    import SQL._

    val a = Account.checkingAccount("a-123456", "debasish ghosh", Some(today), None, Balance(100)) match {
      case Right(a) => a
      case Left(_) => throw new Exception("blah")
    }

    // println(insert(a).run.transact(xa).unsafeRunSync())
    get(a.no).to[List].transact(xa).unsafeRunSync().take(5).foreach(println)

    sql"select * from accounts"
      .query[Account]    // Query0[String]
      .to[List]         // ConnectionIO[List[String]]
      .transact(xa)     // IO[List[String]]
      .unsafeRunSync    // List[String]
      .take(5)          // List[String]
      .foreach(println)

  }
}