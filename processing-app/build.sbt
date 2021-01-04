
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.elijordan"

lazy val root = (project in file("."))
  .settings(
    name := "turing-patterns",
    libraryDependencies ++= Seq(
      "org.processing" % "core" % "3.3.7"
    )
  )
