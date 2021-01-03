ThisBuild / name := "zio-ssi"
ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "2.13.4"

lazy val zioVersion = "1.0.3"
lazy val zioActorsVersion = "0.0.8"
lazy val circeVersion = "0.12.3"

lazy val root = project
  .in(file("."))
  .aggregate(did, hydrogen)

lazy val hydrogen = project
  .in(file("hydrogen"))
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio"              % zioVersion,
      "org.bitcoinj"  % "core"              % "0.15",
      "co.libly"      % "hydride-java"      % "1.1.3",

      "dev.zio"       %% "zio-test"         % zioVersion % "test",
      "dev.zio"       %% "zio-test-sbt"     % zioVersion % "test"
    )
  )

lazy val did = project
  .in(file("did"))
  .dependsOn(hydrogen)
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio"       %% "zio-json"         % "0.0.1",

      "dev.zio"       %% "zio-test"         % zioVersion % "test",
      "dev.zio"       %% "zio-test-sbt"     % zioVersion % "test"
    )
  )

lazy val agent = project
  .in(file("agent"))
  .dependsOn(hydrogen, did)
  .settings(
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.1" cross CrossVersion.full)
  )
  .settings(
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-actors"             % zioActorsVersion,
      "dev.zio" %% "zio-actors-persistence" % zioActorsVersion,

      "dev.zio"       %% "zio-test"         % zioVersion % "test",
      "dev.zio"       %% "zio-test-sbt"     % zioVersion % "test"
    )
  )
