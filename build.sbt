organization := "pl.project13.scala"

name := "akka-io-spdy"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.2"

val akkaV = "2.2.3"

val sprayV = "1.2.0"

libraryDependencies += "io.spray"            %   "spray-can"     % sprayV

libraryDependencies += "io.spray"            %   "spray-routing" % sprayV

libraryDependencies += "io.spray"            %   "spray-testkit" % sprayV

libraryDependencies += "com.typesafe.akka"   %%  "akka-actor"    % akkaV

libraryDependencies += "com.typesafe.akka"   %%  "akka-testkit"  % akkaV

libraryDependencies += "org.scalatest"       %% "scalatest"      % "2.0" % "test"


// publishing settings

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

credentials += Credentials(Path.userHome / ".sbt" / "sonatype.properties")

pomExtra := (
<url>http://github.com/ktoso/akka-io-spdy</url>
<licenses>
  <license>
    <name>Apache 2 License</name>
    <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
    <distribution>repo</distribution>
  </license>
</licenses>
<scm>
  <url>git@github.com:ktoso/akka-io-spdy.git</url>
  <connection>scm:git:git@github.com:ktoso/akka-io-spdy.git</connection>
</scm>
<developers>
  <developer>
    <id>ktoso</id>
    <name>Konrad 'ktoso' Malawski</name>
    <url>http://blog.project13.pl</url>
  </developer>
</developers>
<parent>
  <groupId>org.sonatype.oss</groupId>
  <artifactId>oss-parent</artifactId>
  <version>7</version>
</parent>)
