import sbt._

object Dependencies {

  object Versions {
    val Cats       = "2.5.0"
    val CatsEffect = "3.0.2"
    val Fs2        = "3.0.1"
    val FireStore  = "2.2.5"

    val ScalaTest = "3.2.3"
    val Docker    = "0.39.3"
    val Logback   = "1.2.3"
    val Slf4s     = "1.7.26"

    val BetterMonadicFor = "0.3.1"
    val ContextApplied   = "0.1.4"
    val KindProjector    = "0.11.3"
    val MacroParadise    = "2.1.1"
  }

  object Libraries {
    val Cats        = "org.typelevel" %% "cats-core"            % Versions.Cats
    val CatsEffect  = "org.typelevel" %% "cats-effect"          % Versions.CatsEffect
    val Fs2Core     = "co.fs2"        %% "fs2-core"             % Versions.Fs2
    val Fs2Io       = "co.fs2"        %% "fs2-io"               % Versions.Fs2
    val Fs2Reactive = "co.fs2"        %% "fs2-reactive-streams" % Versions.Fs2

    val FireBase = "com.google.cloud" % "google-cloud-firestore" % Versions.FireStore

    val ScalaTest = "org.scalatest" %% "scalatest" % Versions.ScalaTest % Test

    val Docker = "com.dimafeng" %% "testcontainers-scala-scalatest" % Versions.Docker % Test

    val Logging =
      List(
        "ch.qos.logback"  % "logback-classic" % Versions.Logback % Test,
        "ch.timo-schmid" %% "slf4s-api"       % Versions.Slf4s
      )
  }

  object CompilerPlugins {
    val betterMonadicFor = compilerPlugin(
      "com.olegpy" %% "better-monadic-for" % Versions.BetterMonadicFor
    )
    val contextApplied = compilerPlugin(
      "org.augustjune" %% "context-applied" % Versions.ContextApplied
    )
    val kindProjector = compilerPlugin(
      "org.typelevel" %% "kind-projector" % Versions.KindProjector cross CrossVersion.full
    )
    val macroParadise = compilerPlugin(
      "org.scalamacros" % "paradise" % Versions.MacroParadise cross CrossVersion.full
    )
  }
}
