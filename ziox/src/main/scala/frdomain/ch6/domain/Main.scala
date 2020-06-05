package frdomain.ch6.domain

import java.io.IOException
import zio._
import zio.blocking.Blocking

import config._
import common._
import service._
import model.{Account, Balance}
import repository.{AccountRepository, InMemoryAccountRepository, DoobieAccountRepository}

object Main {
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
  

  def main(args: Array[String]): Unit = {
    val prog =
      for {
        _      <- opens
        _      <- credits
        a      <- balanceByAccount

      } yield a

    val banking: ZIO[Blocking, Object, Seq[(String, common.Amount)]] = prog.provideLayer(Layers.live.appLayer) 
    println(Runtime.default.unsafeRun(banking))
  }
}