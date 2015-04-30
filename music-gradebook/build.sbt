name := "Music Gradebook"

version := "3.20"

organization := "com.dbschools"

scalaVersion := "2.11.6"

jetty()

resolvers ++= Seq(
  "Java.net Maven2 Repository"     at "http://download.java.net/maven/2/",
  "Sonatype scala-tools repo"      at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype scala-tools releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype scala-tools snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  "scalaz-bintray"                 at "http://dl.bintray.com/scalaz/releases"
)

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies ++= {
  val liftVersion = "3.0-M5-1"
  Seq(
    "javax.servlet"     %  "servlet-api"        % "2.5" % "compile",
    "com.typesafe.akka" %% "akka-actor"         % "2.3.9" withSources(),
    "org.scalaz"        %% "scalaz-core"        % "7.1.1" withSources(),
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-record"        % liftVersion,
    "net.liftweb"       %% "lift-squeryl-record" % liftVersion exclude("org.squeryl","squeryl"),
    "org.eclipse.jetty" %  "jetty-webapp"       % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "log4j"             %  "log4j"              % "1.2.17",
    "org.specs2"        %% "specs2-core"        % "3.4" % "test",
    "com.h2database"    %  "h2"                 % "1.3.173",
    "org.squeryl"       %% "squeryl"            % "0.9.5-6",
    "postgresql"        %  "postgresql"         % "8.4-702.jdbc4",
    "org.scalaj"        %% "scalaj-time"        % "0.5",
    "com.jolbox"        %  "bonecp"             % "0.7.1.RELEASE" // connection pooling
  )
}

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "code"
  )

fullRunTask(TaskKey[Unit]("load-sample-data", "Loads sample data"), Compile, "com.dbschools.mgb.TestDataMaker")
