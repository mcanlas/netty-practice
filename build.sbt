scalaVersion := "2.13.0"

javaOptions += "-Dio.netty.leakDetectionLevel=ADVANCED"

libraryDependencies += "io.netty" % "netty-all" % "4.1.39.Final"

libraryDependencies += "org.typelevel" %% "cats-effect" % "2.0.0-RC1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
