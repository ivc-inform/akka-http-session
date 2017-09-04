import sbt.Credentials
import sbt.Keys.credentials

import scalariform.formatter.preferences._

lazy val commonSettings = scalariformSettings ++ Seq(
    organization := "com.softwaremill.akka-http-session",
    version := "0.5.2",
    scalaVersion := "2.12.3",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(DoubleIndentClassDeclaration, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
      .setPreference(CompactControlReadability, true)
      .setPreference(SpacesAroundMultiImports, false),
    // Sonatype OSS deployment
    publishTo := {
        val corporateRepo = "http://toucan.simplesys.lan/"
        if (isSnapshot.value)
            Some("snapshots" at corporateRepo + "artifactory/libs-snapshot-local")
        else
            Some("releases" at corporateRepo + "artifactory/libs-release-local")
    },
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    publishMavenStyle := true
)

val akkaHttpVersion = "10.0.10"

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3" % Test

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
      publishArtifact := false,
      name := "akka-http-session")
  .aggregate(core, jwt, example, javaTests)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
      name := "core",
      libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
          "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
          "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
          scalaTest
      )
  )

lazy val jwt: Project = (project in file("jwt"))
  .settings(commonSettings: _*)
  .settings(
      name := "jwt",
      libraryDependencies ++= Seq(
          "org.json4s" %% "json4s-jackson" % "3.5.0",
          scalaTest
      )
  ) dependsOn (core)

lazy val example: Project = (project in file("example"))
  .settings(commonSettings: _*)
  .settings(
      publishArtifact := false,
      libraryDependencies ++= Seq(
          "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
          "ch.qos.logback" % "logback-classic" % "1.1.7",
          "org.json4s" %% "json4s-ext" % "3.5.0"
      ))
  .dependsOn(core, jwt)

lazy val javaTests: Project = (project in file("javaTests"))
  .settings(commonSettings: _*)
  .settings(
      name := "javaTests",
      testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a")), // required for javadsl JUnit tests
      crossPaths := false, // https://github.com/sbt/junit-interface/issues/35
      publishArtifact := false,
      libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
          "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
          "junit" % "junit" % "4.12" % Test,
          "com.novocode" % "junit-interface" % "0.11" % Test,
          scalaTest
      ))
  .dependsOn(core, jwt)

