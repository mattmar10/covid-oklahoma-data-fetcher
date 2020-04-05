import NativePackagerHelper._

name := "mattmar10-covid-data-fetcher"

scalaVersion := "2.13.1"

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.0.1-SNAPSHOT"

val circeVersion = "0.12.3"
val slf4jVersion = "1.7.26"
val awsVersion = "1.11.610"
val awsCdkVersion = "1.22.0"
val lambdaVersion = "1.2.0"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % lambdaVersion,
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.amazonaws" % "aws-java-sdk-sns" % awsVersion,
  "com.amazonaws" % "aws-java-sdk-s3" % awsVersion,
  //"org.slf4j" % "slf4j-simple" % "1.7.26",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

enablePlugins(JavaAppPackaging)

coverageMinimum := 95
coverageFailOnMinimum := true
coverageHighlighting := true
coverageEnabled.in(Test, test) := false

/**
  * Native Packager config for AWS Lambda:
  * 1.) no top level directory in the zip
  * 2.) this app itself should not be jar'd, the classes and resources should be at the top dir
  * 3.) No docs are needed - just the app + jars
  * 4.) All dependency jars in /lib EXCEPT this apps jar itself (which is built by native packager)
  */
topLevelDirectory := None
mappings in Universal ++= {
  (packageBin in Compile).value
  val t = target.value
  val dir = t / "scala-2.13" / "classes"
  (dir.allPaths --- dir) pair relativeTo(dir)
}
mappings in (Compile, packageDoc) := Seq()
mappings in Universal := {
  (mappings in Universal).value filter {
    case (_, fname) => !fname.endsWith(s"${name.value}-${version.value}.jar")
  }
}
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
