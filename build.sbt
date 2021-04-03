import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "zio_sample",
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.2",
      "org.scalatest" %% "scalatest" % "3.2.2" % "test",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.typelevel" %% "cats-core" % "2.1.1",

      "dev.zio" %% "zio" % "1.0.5",
    )
  )

