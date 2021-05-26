ThisBuild / version := "0.1.0"
ThisBuild / scalaVersion := "3.0.0"

lazy val zioVersion = "1.0.8"
lazy val nimbusJoseJwtVersion = "9.9.3"

lazy val root = project
  .in(file("."))
  .aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "zio-ssi",

    libraryDependencies ++= Seq(
      "com.nimbusds"    % "nimbus-jose-jwt"     % nimbusJoseJwtVersion,
      "dev.zio"         %% "zio"                % zioVersion
    )
  )

lazy val coreTest = project
  .in(file("core-test"))
  .dependsOn(core)
  .settings(
    name := "core-test",

    libraryDependencies ++= Seq(
      "com.nimbusds"    % "nimbus-jose-jwt"     % nimbusJoseJwtVersion,
      "dev.zio"         %% "zio"                % zioVersion,

      "dev.zio"         %% "zio-test"           % zioVersion            % Test,
      "dev.zio"         %% "zio-test-sbt"       % zioVersion            % Test
    ),

    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
