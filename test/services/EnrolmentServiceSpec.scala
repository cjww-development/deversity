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

package services

import com.cjwwdev.mongo.responses.MongoSuccessUpdate
import com.cjwwdev.security.obfuscation.Obfuscation._
import com.cjwwdev.implicits.ImplicitDataSecurity._
import common.UpdateFailedException
import helpers.other.AccountEnums
import helpers.services.ServiceSpec
import models.RegistrationCode
import org.joda.time.DateTime

class EnrolmentServiceSpec extends ServiceSpec {

  val testService = new EnrolmentService {
    override val userAccountRepository = mockUserAccountRepo
    override val registrationCodeRepository = mockRegCodeRepo
  }

  val testUserId = generateTestSystemId(USER)

  "createDeversityId" should {
    "return a deversity id" in {
      val testDevId = s"deversity-$uuid"

      mockCreateDeversityId(deversityId = testDevId)

      awaitAndAssert(testService.createDeversityId(generateTestSystemId(USER))) {
        _ mustBe testDevId.encrypt
      }
    }
  }

  "getEnrolment" should {
    "return a DeversityEnrolment" in {
      val testAcc = testUserAccount(AccountEnums.student)

      mockGetUserBySelector(returned = testAcc)

      awaitAndAssert(testService.getEnrolment(generateTestSystemId(USER))) {
        _ mustBe testAcc.deversityDetails
      }
    }
  }

  "updateDeversityEnrolment" should {
    val testDeversityEnrolment = testTeacherEnrolment

    "return a MongoSuccessUpdate" in {
      mockUpdateDeversityEnrolment(updated = true)

      awaitAndAssert(testService.updateDeversityEnrolment(testUserId, testDeversityEnrolment)) {
        _ mustBe MongoSuccessUpdate
      }
    }

    "return an MongoFailedUpdate" in {
      mockUpdateDeversityEnrolment(updated = false)

      intercept[UpdateFailedException](await(testService.updateDeversityEnrolment(testUserId, testDeversityEnrolment)))
    }
  }

  "getRegistrationCode" should {
    val testRegCode = RegistrationCode(testUserId, "testRegCode", DateTime.now)

    "return a registration code" in {
      mockGetRegistrationCode(testRegCode)

      awaitAndAssert(testService.getRegistrationCode(testUserId)) {
        _ mustBe testRegCode
      }
    }
  }

  "generateRegistrationCode" should {
    "return true" when {
      "the generation of the reg code was successful" in {
        mockGenerateRegistrationCode(generated = true)

        awaitAndAssert(testService.generateRegistrationCode(testUserId)) {
          assert(_)
        }
      }
    }

    "return false" when {
      "there was a problem generating a reg code" in {
        mockGenerateRegistrationCode(generated = false)

        awaitAndAssert(testService.generateRegistrationCode(testUserId)) { generated =>
          assert(!generated)
        }
      }
    }
  }

  "lookupRegistrationCode" should {
    "return a user id" in {
      mockLookupUserIdByRegCode(userId = "user-test-id")

      awaitAndAssert(testService.lookupRegistrationCode("testRegCode")) {
        _ mustBe "user-test-id"
      }
    }
  }
}
