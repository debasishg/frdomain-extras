import sbt._
import Keys._
import Versions._

object Dependencies {

  object Misc {
    val newtype           = "io.estatico"                  %% "newtype"                          % newtypeVersion
    val refinedCore       = "eu.timepit"                   %% "refined"                          % refinedVersion
    val refinedCats       = "eu.timepit"                   %% "refined-cats"                     % refinedVersion
    val squants           = "org.typelevel"                %% "squants"                          % squantsVersion

  }

  object Cats {
    val cats              = "org.typelevel"                %%   "cats-core"                      % catsVersion
    val catsEffect        = "org.typelevel"                %%   "cats-effect"                    % catsEffectVersion
    val catsMtl           = "org.typelevel"                %%   "cats-mtl-core"                  % catsMtlVersion
  }

  object Monix {
    val monix             = "io.monix"                     %% "monix"                            % monixVersion
  }

  object Zio {
    val zio               = "dev.zio"                      %% "zio"                              % zioVersion
    val ziotest           = "dev.zio"                      %% "zio-test"                         % zioVersion % "test"
    val ziotestsbt        = "dev.zio"                      %% "zio-test-sbt"                     % zioVersion % "test"
    val zioInteropCats    = "dev.zio"                      %% "zio-interop-cats"                 % "2.5.1.0"
    val zioLogging        = "dev.zio"                      %% "zio-logging"                      % zioLoggingVersion
    val zioLoggingSlf4j   = "dev.zio"                      %% "zio-logging-slf4j"                % zioLoggingVersion
    val log4jAPI          = "org.apache.logging.log4j"      % "log4j-api"                        % log4j2Version
    val log4jCore         = "org.apache.logging.log4j"      % "log4j-core"                       % log4j2Version
    val log4jSlf4jImpl    = "org.apache.logging.log4j"      % "log4j-slf4j-impl"                 % log4j2Version
    val pureconfig        = "com.github.pureconfig"        %% "pureconfig"                       % pureconfigVersion
  }

  object Doobie {
    val doobieCore        = "org.tpolecat"                 %% "doobie-core"                      % doobieVersion
    val doobieH2          = "org.tpolecat"                 %% "doobie-h2"                        % doobieVersion
    val doobieHikari      = "org.tpolecat"                 %% "doobie-hikari"                    % doobieVersion
    val doobiePostgres    = "org.tpolecat"                 %% "doobie-postgres"                  % doobieVersion
  }

  object Skunk {
    val skunkCore         = "org.tpolecat"                 %% "skunk-core"                       % skunkVersion
    val skunkCirce        = "org.tpolecat"                 %% "skunk-circe"                      % skunkVersion
  }

  object Ciris {
    val cirisCore         = "is.cir"                       %% "ciris"                            % cirisVersion
    val cirisEnum         = "is.cir"                       %% "ciris-enumeratum"                 % cirisVersion
    val cirisRefined      = "is.cir"                       %% "ciris-refined"                    % cirisVersion
  }

  val flywayDb            = "org.flywaydb"                  % "flyway-core"                      % "7.15.0"

  val kindProjector = compilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjectorVersion cross CrossVersion.full)

  val commonDependencies: Seq[ModuleID] = Seq(Cats.cats, Cats.catsEffect)
  val monixDependencies: Seq[ModuleID] = Seq(Monix.monix)

  val catsIODependencies: Seq[ModuleID] = commonDependencies
  val taglessDependencies: Seq[ModuleID] = commonDependencies ++ Seq(kindProjector) ++ monixDependencies

  val catsMtlDependencies: Seq[ModuleID] = 
    commonDependencies ++ Seq(Cats.catsMtl) ++ Seq(kindProjector) ++ monixDependencies ++ Seq(Misc.newtype, Misc.refinedCore, Misc.refinedCats, Misc.squants) ++ Seq(Skunk.skunkCore, Skunk.skunkCirce) ++ Seq(Ciris.cirisCore, Ciris.cirisEnum, Ciris.cirisRefined)

  val zioDependencies: Seq[ModuleID] = commonDependencies ++ Seq(Zio.zio, Zio.zioLogging, Zio.zioLoggingSlf4j, Zio.log4jAPI, Zio.log4jCore, 
    Zio.log4jSlf4jImpl, Zio.pureconfig, Zio.zioInteropCats, Doobie.doobieCore, Doobie.doobieH2, Doobie.doobieHikari, Doobie.doobiePostgres, flywayDb) 
}
