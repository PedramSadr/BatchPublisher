import sbt._
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import sbtunidoc.Plugin.UnidocKeys._
import sbt._
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.SbtGhPages.GhPagesKeys._
import sbtunidoc.Plugin.UnidocKeys._
import ScalaxbKeys._


val scalaVersionCommon = "2.11.8"

inThisBuild(
  scalaVersion := scalaVersionCommon
)

lazy val buildSettings = Seq(
  organization := "com.adv",
  scalaVersion := scalaVersionCommon,
  version := "0.0.5",

  credentials  += Credentials(Path.userHome / ".sbt" / ".credentials")
)

///If Marketcetera updates their quickfix implementation, then add this back:
//resolvers +=  "Marketcetera" at "http://repo.marketcetera.org/maven"

//resolvers ++=  Seq("Confluent Maven Repo" at "http://packages.confluent.io/maven/")

lazy val artifactoryRoot = new Object {
  val london = "http://sdpvvrln101.scglobal.ad.adv.com:8080/artifactory"
  val toronto = "http://af.cds.bns:8081/artifactory"
}

val akka = "2.4.0"
val scalaz = "7.1.4"
val logback = "1.1.3"
val slayer = "2.4.5"

val ignoredFiles = Set("")

mainClass in Compile := Some("Main")
lazy val assemblySettings = Seq(
  // sbt-assembly
//  assemblyJarName.in(assembly) := (assemblyJarName.in(assembly).value + scalaVersion.value).replace("-","_"),
  artifact in(Compile, assembly) ~= (_.copy(`classifier` = Some("assembly"))),
  test in assembly := {},
  publish in packageBin := false,
  mainClass in assembly := Some("Main")
) ++ addArtifact(artifact in (Compile, assembly), assembly)


lazy val commonSettings = StandardProject.newSettings ++
  Seq(
    scalacOptions ++= "-deprecation" ::
      "-encoding" :: "UTF-8" ::
      "-feature" ::
      "-unchecked" ::
      "-Xlint" ::
      "-Yno-adapted-args" ::
      "-Ywarn-dead-code" ::
      "-Ywarn-numeric-widen" ::
      "-Ywarn-value-discard" ::
      "-Xfuture" ::
      Nil,
    exportJars := true,

    // Filter out configuration files that are to be supplied separate of the jar.
    mappings in (Compile, packageBin) ~= { _.filter( k => !ignoredFiles.contains(k._1.getName)) },

    resourceManaged in Compile <<= sourceDirectory(new File(_, "main/managed")),
    unmanagedSourceDirectories in Compile <+= sourceDirectory(new File(_, "main/managed")),
    unmanagedBase <<= baseDirectory(base => base / "lib"), // Omit libraries

    // scalastyle-sbt-plugin
    scalastyleFailOnError := true,

//    moduleName <<= moduleName(n => s"guile-$n"),

    // Artifactory
    publishTo <<= version(v =>
      if (v.trim endsWith "SNAPSHOT") Some(BnsResolvers.localSnapshot)
      else Some(BnsResolvers.localRelease)
    ),
    checksums := Nil,
    scmInfo := Some(ScmInfo(url("http://bitbucket.agile.bns/scm/gfi/kafka-batch-publisher.git"), "git@bitbucket.agile.bns:7999/gfi/kafka-batch-publisher.git"))
  )
//http://s3927712@bitbucket.agile.bns/scm/gfi/kafka-batch-publisher.git

lazy val guileSettings = buildSettings ++ commonSettings

lazy val slayerDeps = {
  Seq(
    "com.adv" %% "slayer-core" % slayer,
    "com.adv" %% "slayer-logging" % slayer,
    "com.adv" %% "slayer-db" % slayer
  )
}

lazy val projectLibs = {
  Seq(
    "com.adv" % "guile-ion_2.11" % "1.5.0",  //TODO: this project (i.e. guile-kafka) should exist as a library
    //and should be used in another separate "mini project" to do the
    // "stitching together"  of ion and kafka plugins.  In other words
    // including the 'guile-ion' dependency is wrong; we should really be including just core and app.
    "org.scalatest" %% "scalatest" % "2.2.5" % "test",
    "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.4" % "test",
    "org.apache.kafka" % "kafka_2.11" % "0.10.2.0" exclude("log4j","log4j") exclude("org.slf4j", "slf4j-log4j12") force(),
    //"org.apache.kafka" % "kafka_2.11" % "0.9.0.1" exclude("log4j","log4j") exclude("org.slf4j", "slf4j-log4j12") force(),
    "org.apache.avro" % "avro" % "1.8.1",
    "io.confluent" % "kafka-avro-serializer" % "3.0.1" exclude("log4j","log4j") exclude("org.slf4j", "slf4j-log4j12") force(),
    "io.confluent" % "kafka-connect-avro-converter" % "3.0.1" exclude("log4j","log4j") exclude("org.slf4j", "slf4j-log4j12") force(),
    "net.liftweb" %% "lift-json" % "2.6",
    "commons-codec" % "commons-codec" % "1.10",
    "org.scalikejdbc" %% "scalikejdbc"       % "2.5.0",
    "com.bns.sa" % "idgenerator" % "1.1",
    "com.bns.sa" % "sa-enums" % "1.6-b18",
    "com.bns.sa" % "sa-schema-v1" % "1.7" exclude("org.apache.avro","avro-tools")

  ) ++ slayerDeps
}


// Needed to unconfuse sbt for not having a root project
// Add assemblySettings to those subprojects you want to be assembled,
// or add .disablePlugins(AssemblyPlugin) for the subprojects that should not get assembled
// lazy val guile =
//   .settings(buildSettings)
//   .settings(noPublishSettings)
//   .disablePlugins(AssemblyPlugin)
//   // TeamCity
//   .settings(initialize := { println(s"##teamcity[buildNumber '${version.value}']") })
//   .aggregate(kafka)

lazy val kafka_batch_producer = (project in file("."))
  .settings(guileSettings ++ assemblySettings)
  .settings(initialize := { println(s"##teamcity[buildNumber '${version.value}']") })
  .settings(libraryDependencies ++= projectLibs)



// lazy val noPublishSettings = Seq(
//   publish :=(),
//   publishLocal :=(),
//   publishArtifact := false
// )
