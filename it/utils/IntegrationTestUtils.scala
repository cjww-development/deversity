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
package utils


import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.test.Helpers._
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import repositories.{OrgAccountRepository, RegistrationCodeRepository, UserAccountRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}
import scala.util.Random

trait IntegrationTestUtils extends PlaySpec with Fixtures with GuiceOneServerPerSuite with ScalaFutures with IntegrationPatience
  with BeforeAndAfterEach with BeforeAndAfterAll {

  val wireMockPort = 11111
  val wireMockHost = "localhost"
  val wireMockUrl  = s"http://$wireMockHost:$wireMockPort"
  val appUrl       = s"http://localhost:$port/deversity"

  val additionalConfiguration = Map(
    "microservice.external-services.auth-microservice.domain" -> s"$wireMockUrl/auth",
    "microservice.external-services.session-store.domain"     -> s"$wireMockUrl/session-store"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(additionalConfiguration)
    .build

  val wmConfig     = wireMockConfig().port(wireMockPort)
  val wmServer     = new WireMockServer(wmConfig)

  def startWmServer(): Unit = {
    wmServer.start()
    WireMock.configureFor(wireMockHost, wireMockPort)
  }

  def stopWmServer(): Unit = {
    wmServer.stop()
  }

  def resetWm(): Unit = {
    WireMock.reset()
  }

  def wmGet(url: String, statusCode: Int, responseBody: String): StubMapping = {
    stubFor(get(urlMatching(url))
      .willReturn(
        aResponse()
          .withStatus(statusCode)
          .withBody(responseBody)
      )
    )
  }

  override def beforeEach(): Unit = resetWm()

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWmServer()
  }

  override def afterEach(): Unit = afterITest()

  override def afterAll(): Unit = {
    afterITest()
    stopWmServer()
    super.afterAll()
  }

  lazy val userAccountRepository = app.injector.instanceOf(classOf[UserAccountRepository])
  lazy val orgAccountRepository  = app.injector.instanceOf(classOf[OrgAccountRepository])
  lazy val regCodeRepository     = app.injector.instanceOf(classOf[RegistrationCodeRepository])

  lazy val ws = app.injector.instanceOf(classOf[WSClient])

  def client(url: String): WSRequest = ws.url(url).withHeaders(
    "appId"       -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130",
    "cookieId"    -> testCookieId,
    CONTENT_TYPE  -> TEXT
  )

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, 5.seconds)

  def afterITest(): Unit = {
    userAccountRepository.collection.flatMap(_.remove(BSONDocument("userName" -> "tUserName")))
    orgAccountRepository.collection.flatMap(_.remove(BSONDocument("orgUserName" -> testOrgAccount.orgUserName)))
    regCodeRepository.collection.flatMap(_.drop(failIfNotFound = false))
  }

  private val regCodeLength       = 6
  private val regCodeAllowedChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  def generateRegistrationCode: String = {
    val regCode = new StringBuilder
    for(_ <- 1 to regCodeLength) {
      val randomNum = Random.nextInt(regCodeAllowedChars.length)
      regCode.append(regCodeAllowedChars(randomNum))
    }
    regCode.toString
  }
}
