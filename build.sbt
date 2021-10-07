name := "frdomain-extras"

// global settings for this build
ThisBuild / version := "0.0.1"
ThisBuild / organization := "frdomain"
ThisBuild / scalaVersion := Versions.scalaVersion
ThisBuild / evictionErrorLevel := Level.Warn

lazy val catsio = (project in file("./cats-io"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.catsIODependencies)

  .settings (
    run / fork := true,
    Compile / mainClass := Some("frdomain.ch6.domain.app.App"),
    addCommandAlias("catsio", "catsio/run")
  )

lazy val tagless = (project in file("./tagless"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.taglessDependencies)

  .settings (
    run / fork := true,
    Compile / mainClass := Some("frdomain.ch6.domain.monixtask.app.App"),
    addCommandAlias("tagless", "tagless/run")
  )

lazy val mtl = (project in file("./mtl"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.catsMtlDependencies)
  .settings(scalacOptions += "-Ymacro-annotations")

  .settings (
    run / fork := true,
    Compile / mainClass := Some("frdomain.ch6.domain.io.app.Main"),
    addCommandAlias("mtl", "mtl/run")
  )

lazy val ziox = (project in file("./ziox"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.zioDependencies)

  .settings (
    run / fork := true,
    addCommandAlias("ziox", "ziox/run")
  )

lazy val root = (project in file(".")).
    aggregate(catsio, tagless, mtl, ziox)

