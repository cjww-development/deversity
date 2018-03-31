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
package app

import com.cjwwdev.security.encryption.DataSecurity
import models.{DeversityEnrolment, RegistrationCode}
import models.formatters.MongoFormatting
import play.api.libs.json.JsSuccess
import utils.{IntegrationSpec, IntegrationStubbing}
import play.api.test.Helpers._

class EnrolmentControllerISpec extends IntegrationSpec with IntegrationStubbing {

  implicit val formatter = DeversityEnrolment.format(MongoFormatting)

  val testRegCode = generateRegistrationCode

  s"/$testUserId/create-deversity-id" should {
    "return an Ok" when {
      "a deversity id has been created for a user" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/$testUserId/create-deversity-id").patch("abc"))
        result.status mustBe OK

        given.user.individualUser.getUser.\("enrolments").\("deversityId").as[String].contains("deversity") mustBe true
      }
    }

    "return a Conflict" when {
      "the user already has a deversity id" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.hasDeversityId
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/$testUserId/create-deversity-id").patch("abc"))
        result.status mustBe CONFLICT
      }
    }

    "return a NotFound" when {
      "the user doesn't exist" in {
        given
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/$testUserId/create-deversity-id").patch("abc"))
        result.status mustBe NOT_FOUND
      }
    }
  }

  s"/enrolment/$testUserId/deversity" should {
    "return an Ok" when {
      "the deversity enrolment for the user has been found" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/enrolment/$testUserId/deversity").get)
        result.status mustBe OK
        DataSecurity.decryptIntoType[DeversityEnrolment](result.body) mustBe
          JsSuccess(testUserAccount(AccountEnums.teacher).deversityDetails.get)
      }
    }

    "return a NotFound" when {
      "the deversity id for the user isn't defined" in {
        given
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/enrolment/$testUserId/deversity").get)
        result.status mustBe NOT_FOUND
      }
    }
  }

  s"/user/$testUserId/generate-registration-code" should {
    "return an Ok" when {
      "a registration code has been generated for the user" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/generate-registration-code").head)
        result.status mustBe OK
      }
    }
  }

  s"/user/$testUserId/fetch-registration-code" should {
    "return an Ok" when {
      "a registration code has been fetched" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.hasRegistrationCode(testUserId, testRegCode)
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/fetch-registration-code").get)
        result.status mustBe OK
        DataSecurity.decryptIntoType[RegistrationCode](result.body).get.code mustBe testRegCode
      }
    }
  }

  s"/user/$testUserId/lookup/$testRegCode/lookup-reg-code" should {
    "return an Ok" when {
      "a registration code has been found for the user" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.hasRegistrationCode(testUserId, testRegCode)
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/lookup/$testRegCode/lookup-reg-code").get)
        result.status mustBe OK
      }
    }
  }
}
