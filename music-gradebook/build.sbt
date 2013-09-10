name := "Music Gradebook"

version := "3.0.0"

organization := "com.dbschools"

scalaVersion := "2.10.1"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "http://oss.sonatype.org/content/repositories/releases",
                "Java.net Maven2 Repository"     at "http://download.java.net/maven/2/"
                )

seq(com.github.siasia.WebPlugin.webSettings :_*)

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.5"
  Seq(
    "org.scalaz"        %  "scalaz-core_2.10"   % "7.0.3" withSources(),
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-mapper"        % liftVersion        % "compile",
    "net.liftmodules"   %%  "lift-jquery-module_2.5" % "2.4",
    "net.liftweb"       %% "lift-record"        % liftVersion,
    "net.liftweb"       %% "lift-squeryl-record" % liftVersion exclude("org.squeryl","squeryl"),
    "net.liftmodules"   %% "widgets"            % "2.5-RC4-1.3" % "compile" withSources(),
    "org.eclipse.jetty" %  "jetty-webapp"       % "9.0.5.v20130815"  % "container,test",
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "ch.qos.logback"    %  "logback-classic"    % "1.0.13",
    "org.specs2"        %  "specs2_2.10"        % "2.2" % "test",
    "com.h2database"    %  "h2"                 % "1.3.173",
    "org.squeryl"       %% "squeryl"            % "0.9.5-6",
    "postgresql"        %  "postgresql"         % "8.4-702.jdbc4",
    "org.scala-tools.time" % "time_2.9.1"       % "0.5",
    "com.jolbox"        %  "bonecp"             % "0.7.1.RELEASE" // connection pooling
  )
}

fullRunTask(TaskKey[Unit]("load-sample-data", "Loads sample data"), Compile, "com.dbschools.mgb.TestDataMaker")
