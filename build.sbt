organization := "com.github.scullxbones"

version := "0.0.1"

scalaVersion := "2.11.8"

lazy val grunt = taskKey[Unit]("Runs grunt")

grunt := {
    val logger = streams.value.log
    logger.info("Executing task grunt")
    "npm install" ! logger
    "./node_modules/grunt-cli/bin/grunt sbt" ! logger
}

unmanagedSources <<= (unmanagedSources in Compile) dependsOn grunt

compile <<= (compile in Compile) dependsOn grunt

unmanagedResourceDirectories in Compile += baseDirectory.value / "src/main/webapp"

// watchSources <++= baseDirectory map { path => ((path / "src/main/webapp") ** "*.*").get }

// Ref: https://tpolecat.github.io/2014/04/11/scalac-flags.html
scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8", // yes, this is 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture",
    "-Ywarn-unused-import",

    "-target:jvm-1.8"
)
// Remove unused import from console
scalacOptions in(Compile, console) ~= (_ filterNot (_ == "-Ywarn-unused-import"))
scalacOptions in(Test, console) := (scalacOptions in(Compile, console)).value

javacOptions ++= Seq(
    "-source", "1.8",
    "-target", "1.8",
    "-Xlint:unchecked",
    "-Xlint:deprecation"
)

libraryDependencies ++= {
    val liftV = "2.6.3"
    val jettyV = "9.3.8.v20160314"
    val slf4jV = "1.7.19"
    val akkaV = "2.4.4"

    Seq(
        "com.typesafe.akka" %% "akka-stream" % akkaV,
        "org.slf4j" % "slf4j-api" % slf4jV,
        "org.slf4j" % "slf4j-simple" % slf4jV,
        "org.eclipse.jetty" % "jetty-webapp" % jettyV,
        "org.eclipse.jetty" % "jetty-rewrite" % jettyV,
        "net.liftweb" %% "lift-webkit" % liftV
    )
}

mainClass in Compile := Some("bootstrap.liftweb.Jetty")
