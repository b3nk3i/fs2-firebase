import sbt._

object Dependencies {

  object Versions {
    val Cats       = "2.1.1"
    val CatsEffect = "2.1.3"
    val Fs2        = "2.4.2"
    val FireBase   = "6.14.0"
  }

  object Libraries {
    val Cats        = "org.typelevel" %% "cats-core"            % Versions.Cats
    val CatsEffect  = "org.typelevel" %% "cats-effect"          % Versions.CatsEffect
    val Fs2Core     = "co.fs2"        %% "fs2-core"             % Versions.Fs2
    val Fs2Io       = "co.fs2"        %% "fs2-io"               % Versions.Fs2
    val Fs2Reactive = "co.fs2"        %% "fs2-reactive-streams" % Versions.Fs2

    val FireBase = "com.google.firebase" % "firebase-admin" % Versions.FireBase
  }
}
