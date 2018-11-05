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

import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.implicits.ImplicitJsValues._
import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import models.formatters.MongoFormatting
import models.{DeversityEnrolment, RegistrationCode}
import utils.{IntegrationSpec, IntegrationStubbing}

class EnrolmentControllerISpec extends IntegrationSpec with IntegrationStubbing {

  implicit val formatter = DeversityEnrolment.format(MongoFormatting)

  val testRegCode = generateRegistrationCode

  s"/user/$testUserId/create-deversity-id" should {
    "return an Ok" when {
      "a deversity id has been created for a user" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/create-deversity-id").patch("abc"))
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

        val result = await(client(s"$testAppUrl/user/$testUserId/create-deversity-id").patch("abc"))
        result.status mustBe CONFLICT
      }
    }

    "return a NotFound" when {
      "the user doesn't exist" in {
        given
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/create-deversity-id").patch("abc"))
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

        implicit val deObfuscator: DeObfuscator[DeversityEnrolment] = new DeObfuscator[DeversityEnrolment] {
          override def decrypt(value: String): Either[DeversityEnrolment, DecryptionError] = DeObfuscation.deObfuscate(value)
        }

        val result = await(client(s"$testAppUrl/user/$testUserId/enrolment").get)
        result.status                                               mustBe OK
        result.json.get[String]("body").decrypt[DeversityEnrolment] mustBe Left(testUserAccount(AccountEnums.teacher).deversityDetails.get)
      }
    }

    "return a NotFound" when {
      "the deversity id for the user isn't defined" in {
        given
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/enrolment").get)
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

        implicit val deObfuscator: DeObfuscator[RegistrationCode] = new DeObfuscator[RegistrationCode] {
          override def decrypt(value: String): Either[RegistrationCode, DecryptionError] = {
            DeObfuscation.deObfuscate[RegistrationCode](value)
          }
        }

        val result = await(client(s"$testAppUrl/user/$testUserId/fetch-registration-code").get)
        result.status                                                           mustBe OK
        result.json.get[String]("body").decrypt[RegistrationCode].left.get.code mustBe testRegCode
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
