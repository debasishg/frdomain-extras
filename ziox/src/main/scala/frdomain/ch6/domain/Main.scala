package frdomain.ch6.domain

import java.io.IOException
import zio._

import repository.InMemoryAccountRepository
import service._
import model.{Account, Balance}

object Main { 

  def run(): Unit = {

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

  }
}