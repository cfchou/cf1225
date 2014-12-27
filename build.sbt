name := "cf1225"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Typesafe repository releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"    % sprayV,
    //"org.apache.kafka"    %%  "kafka"           % "0.8.1.1",
    "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
    "org.clapper"         %%  "grizzled-slf4j"  % "1.0.2",
    "ch.qos.logback"      %   "logback-classic" % "1.1.2",
    "org.scalacheck"      %%  "scalacheck"      % "1.12.1",
    "org.scalatest"       %%  "scalatest"       % "2.2.1"   % "test"
  )
}
