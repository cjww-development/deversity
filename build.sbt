// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.typesafe.config.ConfigFactory
import scoverage.ScoverageKeys
import scala.util.{Try, Success, Failure}

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

name         := """deversity"""
version      := btVersion
scalaVersion := "2.11.11"
organization := "com.cjww-dev.backends"

lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages  := "<empty>;Reverse.*;models/.data/..*;views.*;models.*;config.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimum           := 80,
  ScoverageKeys.coverageFailOnMinimum     := false,
  ScoverageKeys.coverageHighlighting      := true
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(playSettings ++ scoverageSettings : _*)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    Keys.fork in IntegrationTest := false,
    unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
    parallelExecution in IntegrationTest := false)

PlayKeys.devSettings := Seq("play.server.http.port" -> "9973")

val cjwwDeps: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" % "data-security_2.11"         % "2.8.0",
  "com.cjww-dev.libs" % "reactive-mongo_2.11"        % "3.5.0",
  "com.cjww-dev.libs" % "backend-auth_2.11"          % "2.10.0",
  "com.cjww-dev.libs" % "application-utilities_2.11" % "2.3.0",
  "com.cjww-dev.libs" % "metrics-reporter_2.11"      % "0.5.0"
)

val codeDeps: Seq[ModuleID] = Seq(
  "com.kenshoo" % "metrics-play_2.10" % "2.4.0_0.4.0",
  filters
)

val testDeps: Seq[ModuleID] = Seq(
  "com.cjww-dev.libs" % "testing-framework_2.11" % "0.1.0"
)

libraryDependencies ++= cjwwDeps
libraryDependencies ++= codeDeps
libraryDependencies ++= testDeps

resolvers += "cjww-dev" at "http://dl.bintray.com/cjww-development/releases"

herokuAppName in Compile := "cjww-deversity-backend"

bintrayOrganization                   := Some("cjww-development")
bintrayReleaseOnPublish in ThisBuild  := true
bintrayRepository                     := "releases"
bintrayOmitLicense                    := true
