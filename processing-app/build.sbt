
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "io.elijordan"

lazy val root = (project in file("."))
  .settings(
    name := "processing-app",
    libraryDependencies ++= Seq(
      "org.processing" % "core" % "3.3.7",
      "com.github.wendykierp" % "JTransforms" % "3.1"
    )
  )
