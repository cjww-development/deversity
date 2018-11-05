/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.heroku.sbt.HerokuPlugin.autoImport.herokuAppName
import com.typesafe.config.ConfigFactory
import sbt.Keys.scalaVersion
import scoverage.ScoverageKeys

import scala.util.{Failure, Success, Try}

val appName = "deversity"

val btVersion: String = Try(ConfigFactory.load.getString("version")) match {
  case Success(ver) => ver
  case Failure(_)   => "0.1.0"
}

val scoverageSettings = Seq(
  ScoverageKeys.coverageExcludedPackages  := "<empty>;Reverse.*;models/.data/..*;views.*;models.*;common.*;.*(AuthService|BuildInfo|Routes).*",
  ScoverageKeys.coverageMinimum           := 80,
  ScoverageKeys.coverageFailOnMinimum     := false,
  ScoverageKeys.coverageHighlighting      := true
)

val mavenResolvers: Seq[MavenRepository] = Seq(
  "cjww-dev" at "https://dl.bintray.com/cjww-development/releases",
  "breadfan" at "https://dl.bintray.com/breadfan/maven"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala)
  .settings(scoverageSettings)
  .configs(IntegrationTest)
  .settings(PlayKeys.playDefaultPort := 9973)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    version                                       :=  btVersion,
    scalaVersion                                  :=  "2.12.7",
    organization                                  :=  "com.cjww-dev.apps",
    resolvers                                     ++= mavenResolvers,
    libraryDependencies                           ++= AppDependencies(),
    libraryDependencies                           +=  filters,
    herokuAppName              in Compile         :=  "cjww-deversity-backend",
    bintrayOrganization                           :=  Some("cjww-development"),
    bintrayReleaseOnPublish    in ThisBuild       :=  true,
    bintrayRepository                             :=  "releases",
    bintrayOmitLicense                            :=  true,
    fork                       in IntegrationTest :=  false,
    unmanagedSourceDirectories in IntegrationTest :=  (baseDirectory in IntegrationTest)(base => Seq(base / "it")).value,
    parallelExecution          in IntegrationTest :=  false
  )
