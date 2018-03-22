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
import play.api.libs.json.{JsSuccess, JsValue, Json}
import play.api.test.Helpers._
import utils.{IntegrationSpec, IntegrationStubbing}

class UtilitiesControllerISpec extends IntegrationSpec with IntegrationStubbing {

  s"/utilities/$testOrgId/pending-deversity-enrolments" should {
    "return an Ok" when {
      "the count has been calculated" in {
        given
          .user.orgUser.isSetup
          .user.individualUser.isSetup
          .user.orgUser.isAuthorised

        val result = await(client(s"$testAppUrl/utilities/$testOrgId/pending-deversity-enrolments").get)
        result.status mustBe OK
        DataSecurity.decryptString(result.body).toInt mustBe 1
      }
    }

    "return an Internal server error" when {
      "the given org Id cannot be matched against a held account" in {
        given
          .user.orgUser.isAuthorised

        val result = await(client(s"$testAppUrl/utilities/$testOrgId/pending-deversity-enrolments").get)
        result.status mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  s"/user/$testUserId/school/$testDeversityId/details" should {
    "return an Ok" when {
      "school details have been found" in {
        given
          .user.individualUser.isSetup
          .user.orgUser.isSetup
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/school/${testDeversityId.encrypt}/details").get)
        result.status mustBe OK
      }
    }

    "return a not found" when {
      "no school details have been found" in {
        given
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/school/${testDeversityId.encrypt}/details").get)
        result.status mustBe NOT_FOUND
      }
    }
  }

  s"/user/$testUserId/teacher/${testDeversityId.encrypt}/school/${testDeversityId.encrypt}/details" should {
    "return an Ok" when {
      "teacher details have been found" in {
        given
          .user.orgUser.isSetup
          .user.individualUser.isSetup
          .user.individualUser.hasDeversityId
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/teacher/${testDeversityId.encrypt}/school/${testDeversityId.encrypt}/details").get)
        result.status mustBe OK
      }
    }

    "return a Not found" when {
      "no teacher details have been found" in {
        given
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/user/$testUserId/teacher/${testDeversityId.encrypt}/school/${testDeversityId.encrypt}/details").get)
        result.status mustBe NOT_FOUND
      }
    }
  }
}
