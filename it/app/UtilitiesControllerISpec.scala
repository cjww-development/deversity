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
import com.cjwwdev.security.obfuscation.Obfuscation._
import utils.{IntegrationSpec, IntegrationStubbing}

class UtilitiesControllerISpec extends IntegrationSpec with IntegrationStubbing {

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
