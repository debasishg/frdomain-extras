package frdomain.ch6.domain

import java.io.IOException
import zio._

import repository.InMemoryAccountRepository
import service._
import model.{Account, Balance}

object Main { 

  def run(): Unit = {
    val banking: ZIO[AccountService, Throwable, Balance] = for {
      a <- open("12345", "account", Some(12.0), None, Checking)
      _ <- credit(a.no, 1000)
      _ <- debit(a.no, 100)
      b <- balance(a.no)
    } yield b

    val horizontal = InMemoryAccountRepository.layer >>> AccountService.live
    val program = banking.provideLayer(horizontal).orDie 
    println(Runtime.default.unsafeRun(program))
  }
}