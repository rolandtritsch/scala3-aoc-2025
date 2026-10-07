// scala3-aoc-2025 - Advent of Code 2025 solutions in Scala 3.
// Based on the scala3-aoc-2024 (Mill) blueprint, migrated to sbt.

ThisBuild / scalaVersion := "3.9.0"

// Required by sbt-scalafix semantic rules (see .scalafix.conf).
ThisBuild / semanticdbEnabled := true
ThisBuild / scalafixDependencies += "com.github.xuwei-k" %% "scalafix-rules" % "0.6.30"

lazy val root = (project in file(".")).settings(
  name := "scala3-aoc-2025",
  scalacOptions ++= Seq("-Wunused:imports", "-Werror", "-deprecation"),
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "ch.qos.logback" % "logback-classic" % "1.5.18",
    "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
    "org.scala-lang.modules" %% "scala-collection-contrib" % "0.4.0",
    "org.scala-graph" %% "graph-core" % "2.0.3",
    "org.scalanlp" %% "breeze" % "2.1.0",
    "org.tritsch" %% "scala-corner" % "1.0.4",
    "org.scalameta" %% "munit" % "1.1.1" % Test,
    "org.scalameta" %% "munit-scalacheck" % "1.1.0" % Test,
    "org.typelevel" %% "spire" % "0.18.0" % Test,
  ),
  // Note: `com.eed3si9n.eval` (scala toolbox) from the 2024 blueprint is
  // intentionally dropped: it was only used by Day24 and pins artifacts
  // to an old Scala minor version.
  testFrameworks += new TestFramework("munit.Framework"),
  Test / fork := true,
  Test / javaOptions ++= Seq("-Xss1G", "-Xmx10G"),
  // Uncomment to focus on / skip tagged tests (see Day00Test for tags).
  // Test / testOptions += Tests.Argument("+l", "--include-tags=only"),
  // Test / testOptions += Tests.Argument("+l", "--exclude-tags=slow"),
)

Test / testOptions += Tests.Argument("-l", "--exclude-tags=ignore")
