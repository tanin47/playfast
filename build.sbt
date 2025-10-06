import tanin.play.svelte.SbtSvelte.autoImport.SvelteKeys.svelte

name := """playfast"""
organization := "tanin.play"

version := "1.0-SNAPSHOT"

import _root_.scalafix.sbt.{BuildInfo => ScalafixBuildInfo}

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb, SbtSvelte, SbtPostcss)

Global / onChangedBuildSource := ReloadOnSourceChanges

scalaVersion := "3.3.5"
semanticdbEnabled := true
semanticdbVersion := scalafixSemanticdb.revision

scalacOptions ++= Seq(
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-Xfatal-warnings", // Fail if there's a warning.
  "-Wnonunit-statement", // Don't allow unused non-Unit expression.
  // Silence warnings on the generated code (e.g. from Play) because we don't have control over it.
  // Also, silence the warnings on the test code.
  "-Wconf:msg=.*unused value of type.*&src=(target|test)/.*:silent",
  "-Wconf:msg=.*unused import.*&src=target/.*:silent",
  "-Wunused:imports" // Warn for unused imports.
)

libraryDependencies ++= Seq(
  guice,
  ws,
  "io.github.tanin47" %% "play3-json-form" % "1.2.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  "de.leanovate.play-mockws" %% "play-mockws-3-0" % "3.1.0" % Test,
  "org.playframework" %% "play-slick" % "6.2.0",
  "org.playframework" %% "play-slick-evolutions" % "6.2.0",
  "com.github.tminglei" %% "slick-pg" % "0.23.1",
  "org.postgresql" % "postgresql" % "42.7.7",
  "org.springframework.security" % "spring-security-crypto" % "6.5.2",
  ("ch.epfl.scala" %% "scalafix-core" % ScalafixBuildInfo.scalafixVersion).cross(
    CrossVersion.for3Use2_13
  ) % ScalafixConfig
)

TwirlKeys.templateImports += "framework.Jsonable._"

pipelineStages ++= Seq(postcss, svelte, gzip, digest)
TestAssets / pipelineStages := Seq(postcss, svelte)
Assets / pipelineStages := Seq.empty

DigestKeys.indexPath := Some("javascripts/versionedAssets.js")
DigestKeys.indexWriter ~= { writer => index => s"var VERSIONED_ASSETS = ${writer(index)};" }

postcss / PostcssKeys.binaryFile := (new File(".") / "node_modules" / ".bin" / "postcss").getAbsolutePath
postcss / PostcssKeys.assetPath := "stylesheets/tailwindbase.css"

svelte / SvelteKeys.webpackBinary := (new File(".") / "node_modules" / ".bin" / "webpack").getAbsolutePath
svelte / SvelteKeys.webpackConfig := (new File(".") / "webpack.config.js").getAbsolutePath

Test / testOptions += Tests.Argument("-oDF") // Show full stack traces and test case durations.

Compile / packageBin / publishArtifact := false
Compile / packageDoc / publishArtifact := false
Compile / packageSrc / publishArtifact := false
Compile / doc / sources := Seq.empty

import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

Docker / maintainer := "@tanin"
Docker / packageName := "personal"
Docker / version := "0"
Docker / daemonUserUid := None
Docker / daemonUser := "daemon"
dockerExposedPorts := Seq(9000)
dockerUsername := Some("tanin47")
dockerUpdateLatest := false
dockerAlias := dockerAlias.value.withTag(Some("play"))
dockerBaseImage := "ghcr.io/graalvm/jdk-community:24"

dockerEntrypoint := dockerEntrypoint.value ++ Seq("-Dconfig.file=./conf/prod.conf")
dockerBuildOptions := dockerBuildOptions.value ++ Seq("--platform=linux/amd64")
