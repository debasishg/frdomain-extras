import sbt._
import Keys._
import Versions._

object Dependencies {

  object Cats {
    val cats              = "org.typelevel"                %%   "cats-core"                      % catsVersion
    val catsEffect        = "org.typelevel"                %%   "cats-effect"                    % catsEffectVersion
    val catsMtl           = "org.typelevel"                %%   "cats-mtl-core"                  % catsMtlVersion
  }

  object Monix {
    val monix             = "io.monix"                     %% "monix"                            % monixVersion
  }

  object Zio {
    val zio               = "dev.zio"                      %% "zio"                              % ZIOVersion
    val ziotest           = "dev.zio"                      %% "zio-test"                         % ZIOVersion % "test"
    val ziotestsbt        = "dev.zio"                      %% "zio-test-sbt"                     % ZIOVersion % "test"
    val zioInteropCats    = "dev.zio"                      %% "zio-interop-cats"                 % "2.0.0.0-RC12"
    val zioLogging        = "dev.zio"                      %% "zio-logging"                      % ZIOLoggingVersion
    val zioLoggingSlf4j   = "dev.zio"                      %% "zio-logging-slf4j"                % ZIOLoggingVersion
    val log4jAPI          = "org.apache.logging.log4j"      % "log4j-api"                        % Log4j2Version
    val log4jCore         = "org.apache.logging.log4j"      % "log4j-core"                       % Log4j2Version
    val log4jSlf4jImpl    = "org.apache.logging.log4j"      % "log4j-slf4j-impl"                 % Log4j2Version
  }

  val macroParadise = compilerPlugin("org.scalamacros" % "paradise" % macroParadiseVersion cross CrossVersion.full)
  val kindProjector = compilerPlugin("org.spire-math" %% "kind-projector" % kindProjectorVersion)

  val commonDependencies: Seq[ModuleID] = Seq(Cats.cats, Cats.catsEffect)
  val monixDependencies: Seq[ModuleID] = Seq(Monix.monix)

  val catsIODependencies: Seq[ModuleID] = commonDependencies
  val taglessDependencies: Seq[ModuleID] = commonDependencies ++ Seq(macroParadise, kindProjector) ++ monixDependencies
  val catsMtlDependencies: Seq[ModuleID] = commonDependencies ++ Seq(Cats.catsMtl) ++ Seq(macroParadise, kindProjector) ++ monixDependencies
  val zioDependencies: Seq[ModuleID] = commonDependencies ++ Seq(Zio.zio, Zio.zioLogging, Zio.zioLoggingSlf4j, Zio.log4jAPI, Zio.log4jCore, Zio.log4jSlf4jImpl) 
}
