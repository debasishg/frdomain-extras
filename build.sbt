name := "frdomain-extras"

// global settings for this build
version in ThisBuild := "0.0.1"
organization in ThisBuild := "frdomain"
scalaVersion in ThisBuild := Versions.scalaVersion


lazy val catsio = (project in file("./cats-io"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.catsIODependencies)

  .settings (
    fork in run := true,
    mainClass in Compile := Some("frdomain.ch6.domain.app.App"),
    addCommandAlias("catsio", "catsio/run")
  )

lazy val tagless = (project in file("./tagless"))
  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.taglessDependencies)

  .settings (
    fork in run := true,
    mainClass in Compile := Some("frdomain.ch6.domain.app.App"),
    addCommandAlias("tagless", "tagless/run")
  )

lazy val root = (project in file(".")).
    aggregate(catsio, tagless)

