ThisBuild / organization := "com.lihaoyi"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / testFrameworks += new TestFramework("utest.runner.Framework")

ThisBuild / resolvers += "integration" at
  "https://scala-ci.typesafe.com/artifactory/scala-integration/"

val commonSettings = Seq(
  libraryDependencies += "com.lihaoyi" %% "utest" % "0.8.2" % Test,
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
)

publish / skip := true  // don't publish root project

// Note that rather than putting source generation in here, I used mill
// to generated the sources, then put them in `src-generated` directories
// and committed them. Probably won't need to be updated very often?
// If we end up wanting to generate the sources here, consult the fastparse
// build for an example.

/*
lazy val scalasql = project.in(file("scalasql"))
  .settings(commonSettings)
  .settings(
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src",
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src-generated",
    Test / unmanagedSourceDirectories += baseDirectory.value / "test" / "src",
    Test / unmanagedResourceDirectories += baseDirectory.value / "test" / "resources",
  )
  .dependsOn(core, query)
*/

lazy val core = project.in(file("scalasql") / "core")
  .settings(commonSettings)
  .settings(
    libraryDependencies += "com.lihaoyi" %% "sourcecode" % "0.3.1",
    libraryDependencies += "com.lihaoyi" %% "geny" % "1.0.0",
    Compile / unmanagedSourceDirectories ++= Seq(baseDirectory.value / "src", baseDirectory.value / "src-2", baseDirectory.value / "src-generated"),
  )

lazy val operations = project.in(file("scalasql") / "operations")
  .settings(commonSettings)
  .settings(
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src",
  )
  .dependsOn(core)

lazy val query = project.in(file("scalasql") / "query")
  .settings(commonSettings)
  .settings(
    Compile / unmanagedSourceDirectories ++= Seq(baseDirectory.value / "src", baseDirectory.value / "src-2", baseDirectory.value / "src-generated"),
  )
  .dependsOn(core)

lazy val scalasql = project.in(file("scalasql"))
  .settings(commonSettings)
  .settings(
    Compile / unmanagedSourceDirectories += baseDirectory.value / "src",
    Test / unmanagedSourceDirectories += baseDirectory.value / "test" / "src",
    Test / unmanagedResourceDirectories += baseDirectory.value / "test" / "resources",
    libraryDependencies += "com.lihaoyi" %% "pprint" % "0.8.1" % Test,
    libraryDependencies += "com.lihaoyi" %% "upickle" % "3.1.3" % Test,
    libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.1" % Test,
    libraryDependencies += "com.github.vertical-blank" % "sql-formatter" % "2.0.4" % Test,
    libraryDependencies += "com.h2database" % "h2" % "2.2.224" % Test,
    libraryDependencies += "org.testcontainers" % "mysql" % "1.19.1" % Test,
    libraryDependencies += "mysql" % "mysql-connector-java" % "8.0.33" % Test,
    libraryDependencies += "org.testcontainers" % "postgresql" % "1.19.1" % Test,
    libraryDependencies += "org.postgresql" % "postgresql" % "42.6.0" % Test,
    libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.43.0.0" % Test,
    libraryDependencies += "com.zaxxer" % "HikariCP" % "5.1.0" % Test,
  )
  .dependsOn(query, operations)
