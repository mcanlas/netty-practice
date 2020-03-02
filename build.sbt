scalaVersion := "2.13.1"

javaOptions += "-Dio.netty.leakDetectionLevel=PARANOID"

libraryDependencies += "io.netty" % "netty-all" % "4.1.46.Final"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Configurations.Test

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.3" % Configurations.Test

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

Test / testOptions +=
      Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
