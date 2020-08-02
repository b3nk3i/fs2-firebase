import Dependencies.Libraries

name := "fs2-firebase"

version := "0.1"

scalaVersion := "2.13.3"

lazy val root = (project in file("."))
  .settings(
    //scalafmtOnCompile := true,
    autoAPIMappings := true,
    scalacOptions ++= Seq("-Ymacro-annotations"),
    libraryDependencies ++= List(
      Libraries.Cats,
      Libraries.CatsEffect,
      Libraries.Fs2Core,
      Libraries.Fs2Io,
      Libraries.Fs2Reactive,
      Libraries.FireBase
    )
  )