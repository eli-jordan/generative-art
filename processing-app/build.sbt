ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.elijordan"

lazy val root = (project in file("."))
  .settings(
    name := "processing-app",
    libraryDependencies ++= Seq(
//      "org.processing" % "core" % "3.3.7",
//      "org.jogamp.jocl" % "jocl-main" % "2.3.2",
      "com.github.wendykierp" % "JTransforms" % "3.1"
    ),
    unmanagedJars in Compile := {
      val libs = baseDirectory.value / "lib"
      val dirs = libs +++ (libs / "jocl-rc") +++ (libs / "jogl-rc") +++ (libs / "processing-core-custom-build")
      (dirs ** "*.jar").classpath
    }
  )
