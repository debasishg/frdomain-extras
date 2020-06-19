package frdomain.ch6.domain
package config

import enumeratum._
import enumeratum.EnumEntry._

object environments {

  sealed abstract class AppEnvironment extends EnumEntry with Lowercase

  object AppEnvironment extends Enum[AppEnvironment] with CirisEnum[AppEnvironment] {
    case object Test extends AppEnvironment
    case object Prod extends AppEnvironment

    val values = findValues
  }
}