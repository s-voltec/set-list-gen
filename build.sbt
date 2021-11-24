/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, v. 2.0.
 */

import com.typesafe.config.ConfigFactory
import slick.codegen.SourceCodeGenerator
import slick.{ model => m }

ThisBuild / organization := "pw.koakoa"
ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.1.0-SNAPSHOT"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

val playConf = ConfigFactory.parseFile(new File("server/conf/application.conf"))

lazy val root = (project in file("."))
  .aggregate(server, client, shared.jvm, shared.js)

lazy val server = project
  .settings(
    scalaJSProjects := Seq(client),
    Assets / pipelineStages  := Seq(scalaJSPipeline),
    pipelineStages := Seq(digest, gzip),
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      guice,
      "com.vmunier" %% "scalajs-scripts" % "1.2.0",
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
      "org.mariadb.jdbc" % "mariadb-java-client" % "1.4.4",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % "test"
    ),
    coverageExcludedFiles := ".*/target/.*",
    Compile / sourceGenerators += slickCodegen,
    slickCodegenDatabaseUrl := playConf.getString("slick.dbs.default.db.url"),
    slickCodegenDatabaseUser := playConf.getString("slick.dbs.default.db.user"),
    slickCodegenDatabasePassword := playConf.getString("slick.dbs.default.db.password"),
    slickCodegenDriver := slick.jdbc.MySQLProfile,
    slickCodegenJdbcDriver := "org.mariadb.jdbc.Driver",
    slickCodegenOutputPackage := "pw.koakoa.setlistgen.models"
  )
  .enablePlugins(PlayScala, WebScalaJSBundlerPlugin, CodegenPlugin)
  .dependsOn(shared.jvm)

lazy val client = project
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.0.0",
      "com.github.japgolly.scalajs-react" %%% "core" % "2.0.0",
      "com.github.japgolly.scalajs-react" %%% "extra" % "2.0.0",
      "org.scalatest" %%% "scalatest" % "3.2.10" % "test"
    ),
    Compile / npmDependencies ++= Seq(
      "react" -> "17.0.2",
      "react-dom" -> "17.0.2"
    ),
    webpackConfigFile := Some(baseDirectory.value / "no-fs.webpack.config.js")
  )
  .enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))
  .settings(
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.10" % "test"
  )
