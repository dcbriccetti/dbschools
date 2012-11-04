name := "Music Gradebook"

version := "3.0.0"

organization := "com.dbschools"

scalaVersion := "2.9.1"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "http://oss.sonatype.org/content/repositories/releases",
                "Java.net Maven2 Repository"     at "http://download.java.net/maven/2/"
                )

seq(com.github.siasia.WebPlugin.webSettings :_*)

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.5-M1"
  Seq(
    "org.scalaz"        %% "scalaz-core"        % "7.0-SNAPSHOT",
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile",
    "net.liftmodules"   %% "lift-jquery-module" % (liftVersion + "-1.0"),
    "net.liftweb"       %% "lift-record"        % liftVersion,
    "net.liftweb"       %% "lift-squeryl-record" % liftVersion exclude("org.squeryl","squeryl"),
    "org.eclipse.jetty" %  "jetty-webapp"       % "8.1.7.v20120910"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    %  "logback-classic"    % "1.0.7",
    "org.specs2"        %% "specs2"             % "1.12.1"           % "test",
    "com.h2database"    %  "h2"                 % "1.3.167",
    "org.squeryl"       %% "squeryl"            % "0.9.5-3",
    "postgresql"        %  "postgresql"         % "8.4-701.jdbc4",
    "org.scala-tools.time" %% "time"            % "0.5",
    "org.mindrot"       %  "jbcrypt"            % "0.3m",
    "com.jolbox"        %  "bonecp"             % "0.7.1.RELEASE" // connection pooling
  )
}

fullRunTask(TaskKey[Unit]("load-sample-data", "Loads sample data"), Compile, "com.dbschools.mgb.TestDataMaker")
