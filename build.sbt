scalaVersion := "2.13.0"

javaOptions += "-Dio.netty.leakDetectionLevel=PARANOID"

libraryDependencies += "io.netty" % "netty-all" % "4.1.41.Final"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Configurations.Test

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Configurations.Test

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

Test / testOptions +=
      Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
