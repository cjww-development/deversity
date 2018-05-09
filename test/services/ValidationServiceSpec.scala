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

import common.{EnrolmentsNotFoundException, RegistrationCodeNotFoundException}
import helpers.other.AccountEnums
import helpers.services.ServiceSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when

import scala.concurrent.Future

class ValidationServiceSpec extends ServiceSpec {

  val testBasicAccount = testUserAccount(AccountEnums.basic)

  val testService = new ValidationService {
    override val userAccountRepository      = mockUserAccountRepo
    override val orgAccountRepository       = mockOrgAccountRepo
    override val registrationCodeRepository = mockRegCodeRepo
  }

  "validateSchool" should {
    "return the schools devId" when {
      "a school has been successfully validated" in {
        val testDevId = generateTestSystemId(ORG)

        mockLookupUserIdByRegCode(userId = testDevId)

        mockGetSchool(fetched = true)

        awaitAndAssert(testService.validateSchool("tSchoolName")) {
          _ mustBe testOrgAccount.deversityId
        }
      }
    }

    "return throw a RegistrationCodeNotFoundException" when {
      "a school has not been validated" in {

        when(mockRegCodeRepo.lookupUserIdByRegCode(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new RegistrationCodeNotFoundException("")))

        intercept[RegistrationCodeNotFoundException](await(testService.validateSchool("tSchoolName")))
      }
    }
  }

  "validateTeacher" should {
    "return the teachers deversityId" when {
      "a teacher has been successfully validated" in {
        val testTeacherAcc = testUserAccount(AccountEnums.teacher)
        val testRegCode    = "testRegCode"

        mockLookupUserIdByRegCode(userId = testOrgAccount.deversityId)

        mockGetSchool(fetched = true)
        when(mockOrgAccountRepo.getSchool(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testOrgAccount))

        mockGetUserBySelector(returned = testTeacherAcc)

        awaitAndAssert(testService.validateTeacher(testRegCode, testOrgDevId)) {
          _ mustBe testTeacherAcc.deversityDetails.get.schoolDevId
        }
      }
    }

    "throw an EnrolmentsNotFoundException" when {
      "the user has no enrolments" in {
        mockLookupUserIdByRegCode(userId = testOrgAccount.deversityId)

        mockGetSchool(fetched = true)

        mockGetUserBySelector(returned = testBasicAccount)

        intercept[EnrolmentsNotFoundException](await(testService.validateTeacher(createTestUserName, testOrgDevId)))
      }
    }
  }
}
