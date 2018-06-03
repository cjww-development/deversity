/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package utils

import akka.util.Timeout
import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.testing.integration.IntegrationTestSpec
import com.cjwwdev.testing.integration.application.IntegrationApplication
import com.cjwwdev.testing.integration.wiremock.WireMockSetup
import play.api.libs.ws.WSRequest
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import repositories.{ClassRoomRepository, OrgAccountRepository, RegistrationCodeRepository, UserAccountRepository}

import scala.concurrent.duration._
import scala.util.Random

trait IntegrationSpec
  extends IntegrationTestSpec
    with TestDataGenerator
    with IntegrationApplication
    with Fixtures
    with WireMockSetup {

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  override val testContextId   = generateTestSystemId(CONTEXT)
  override val testOrgId       = generateTestSystemId(ORG)
  override val testUserId      = generateTestSystemId(USER)
  override val testDeversityId = generateTestSystemId(DEVERSITY)

  override val appConfig = Map(
    "microservice.external-services.auth-microservice.domain" -> s"$wiremockUrl/auth",
    "microservice.external-services.auth-microservice.uri"    -> "/get-current-user/:sessionId",
    "microservice.external-services.session-store.domain"     -> s"$wiremockUrl/session-store",
    "microservice.external-services.session-store.uri"        -> "/session/:contextId/data?key=contextId"
  )

  override val currentAppBaseUrl = "deversity"

  lazy val userAccountRepository = app.injector.instanceOf(classOf[UserAccountRepository])
  lazy val orgAccountRepository  = app.injector.instanceOf(classOf[OrgAccountRepository])
  lazy val regCodeRepository     = app.injector.instanceOf(classOf[RegistrationCodeRepository])
  lazy val classRoomRepository   = app.injector.instanceOf(classOf[ClassRoomRepository])

  val testCookieId = generateTestSystemId(SESSION)

  def client(url: String): WSRequest = ws.url(url).withHeaders(
    "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", testCookieId).encryptType,
    CONTENT_TYPE   -> TEXT
  )

  private def afterITest(): Unit = {
    userAccountRepository.collection.flatMap(_.remove(BSONDocument("userName" -> "tUserName")))
    orgAccountRepository.collection.flatMap(_.remove(BSONDocument("orgUserName" -> testOrgAccount.orgUserName)))
    classRoomRepository.collection.flatMap(_.drop(failIfNotFound = false))
    regCodeRepository.collection.flatMap(_.drop(failIfNotFound = false))
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWm()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWm()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    afterITest()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    afterITest()
    stopWm()
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
