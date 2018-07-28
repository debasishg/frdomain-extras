package frdomain.ch6
package domain
package service
package interpreter

import java.util.{ Date, Calendar }

import cats._
import cats.data._
import cats.implicits._
import cats.instances.all._

import model.{ Account, Balance }
import common._
import repository.AccountRepository

class AccountServiceInterpreter[M[+_]](implicit me: MonadError[M, Throwable]) extends AccountService[M, Account, Amount, Balance] {

  def open(no: String, 
           name: String, 
           rate: Option[BigDecimal],
           openingDate: Option[Date],
           accountType: AccountType) = Kleisli[Valid[M, ?], AccountRepository[M], Account] { (repo: AccountRepository[M]) =>

    EitherT {
      repo.query(no).flatMap {

        case Right(Some(a)) => Left[AccountServiceException, Account](AlreadyExistingAccount(a.no)).pure[M]

        case Right(None)    => accountType match {

          case Checking => createOrUpdate(repo, Account.checkingAccount(no, name, openingDate, None, Balance())) 

          case Savings  => rate map { r =>
            createOrUpdate(repo, Account.savingsAccount(no, name, r, openingDate, None, Balance()))
          } getOrElse {
            Left(RateMissingForSavingsAccount).pure[M]
          }
        }

        case Left(x)        => Left(MiscellaneousDomainExceptions(x)).pure[M]
      }
    }
  }

  private def createOrUpdate(repo: AccountRepository[M], 
    errorOrAccount: ErrorOr[Account]): M[Either[AccountServiceException, Account]] = errorOrAccount match {

    case Left(errs) => Left(MiscellaneousDomainExceptions(errs)).pure[M]
    case Right(a) => repo.store(a).map {
      case Right(acc) => Right(acc)
      case Left(errs) => Left(MiscellaneousDomainExceptions(errs))
    }
  }

  def close(no: String, closeDate: Option[Date]) = Kleisli[Valid[M, ?], AccountRepository[M], Account] { (repo: AccountRepository[M]) =>
    EitherT {
      repo.query(no).flatMap {

        case Right(None) => Left(NonExistingAccount(no)).pure[M]

        case Right(Some(a))    => 
          val cd = closeDate.getOrElse(today)
          createOrUpdate(repo, Account.close(a, cd))

        case Left(x)        => Left(MiscellaneousDomainExceptions(x)).pure[M]
      }
    }
  }

  def debit(no: String, amount: Amount) = up(no, amount, D)
  def credit(no: String, amount: Amount) = up(no, amount, C)

  private trait DC
  private case object D extends DC
  private case object C extends DC

  private def up(no: String, amount: Amount, dc: DC) = Kleisli[Valid[M, ?], AccountRepository[M], Account] { (repo: AccountRepository[M]) =>
    EitherT {
      repo.query(no).flatMap {

        case Right(None) => Left(NonExistingAccount(no)).pure[M]

        case Right(Some(a))    => dc match {
          case D => createOrUpdate(repo, Account.updateBalance(a, -amount))
          case C => createOrUpdate(repo, Account.updateBalance(a, amount))
        }

        case Left(x)        => Left(MiscellaneousDomainExceptions(x)).pure[M]
      }
    }
  }

  def balance(no: String) = Kleisli[Valid[M, ?], AccountRepository[M], Balance] { (repo: AccountRepository[M]) =>
    EitherT {
      repo.balance(no).map { 
        case Left(errs) => Left(MiscellaneousDomainExceptions(errs))
        case Right(b) => Right(b)
      }
    }
  }

  def transfer(from: String, to: String, amount: Amount): AccountOperation[M, (Account, Account)] = for { 
    a <- debit(from, amount)
    b <- credit(to, amount)
  } yield ((a, b))
}
