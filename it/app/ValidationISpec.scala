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
import play.api.libs.json.Json
import utils.{IntegrationSpec, IntegrationStubbing}
import play.api.test.Helpers._

class ValidationISpec extends IntegrationSpec with IntegrationStubbing {

  final val orgRegCode           = generateRegistrationCode
  final val userRegCode          = generateRegistrationCode

  val encodedOrgRegCode    = DataSecurity.encryptString(orgRegCode)
  val encodedUserRegCode   = DataSecurity.encryptString(userRegCode)
  val encodedSchoolDevId   = DataSecurity.encryptString(testOrgAccount.deversityId)

  s"/validation/school/:regCode" should {
    "return an OK" when {
      "the school has been validated" in {
        given
          .user.orgUser.isSetup
          .user.orgUser.hasRegistrationCode(testOrgAccount.orgId, orgRegCode)

        val result = await(client(s"$testAppUrl/validation/school/$encodedOrgRegCode").get())
        result.status mustBe OK
      }
    }

    "return a Not found" when {
      "the school has not been validated" in {
        val result = await(client(s"$testAppUrl/validation/school/$encodedOrgRegCode").get())
        result.status mustBe NOT_FOUND
      }
    }
  }

  "/validation/teacher/:regCode/school/:schoolDevId" should {
    "return an OK" when {
      "the teacher has been validated" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.hasDeversityId
          .user.orgUser.isSetup
          .user.individualUser.hasRegistrationCode(testUserAcc.userId, userRegCode)

        val result = await(client(s"$testAppUrl/validation/teacher/$encodedUserRegCode/school/$encodedSchoolDevId").get())
        result.status mustBe OK
      }
    }

    "return a Not found" when {
      "the teacher has not been validated" in {
        val result = await(client(s"$testAppUrl/validation/teacher/$encodedUserRegCode/school/$encodedSchoolDevId").get())
        result.status mustBe NOT_FOUND
      }
    }
  }
}
