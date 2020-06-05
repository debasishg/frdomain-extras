package frdomain.ch6.domain

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

object DoobieMain {

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