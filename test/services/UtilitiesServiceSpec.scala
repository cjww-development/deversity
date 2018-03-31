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

import common.MissingAccountException
import helpers.other.AccountEnums
import helpers.services.ServiceSpec
import models.TeacherDetails

class UtilitiesServiceSpec extends ServiceSpec {

  val testService = new UtilitiesService {
    override val userAccountRepository = mockUserAccountRepo
    override val orgAccountRepository = mockOrgAccountRepo
  }

  "getPendingEnrolmentCount" should {
    "return a JsValue" when {
      "the count has been calculated" in {
        mockGetSchool(fetched = true)

        mockGetPendingEnrolmentCount

        awaitAndAssert(testService.getPendingEnrolmentCount(generateTestSystemId(ORG))) {
          _ mustBe 1
        }
      }
    }

    "throw a missing account exception" when {
      "the given orgId doesn't matched a held account" in {
        mockGetSchool(fetched = false)

        intercept[MissingAccountException](await(testService.getPendingEnrolmentCount(generateTestSystemId(ORG))))
      }
    }
  }

  "getSchoolDetails" should {
    "return an org details" in {
      mockGetSchool(fetched = true)

      awaitAndAssert(testService.getSchoolDetails("testOrgName")) {
        _ mustBe testOrgDetails
      }
    }
  }

  "getTeacherDetails" should {
    "return a teacher details model" in {
      val testAcc = testUserAccount(AccountEnums.teacher)

      mockGetUserBySelector(returned = testAcc)

      awaitAndAssert(testService.getTeacherDetails("testUserName", "testSchoolName")) {
        _ mustBe TeacherDetails(
          userId   = testAcc.userId,
          title    = testAcc.deversityDetails.get.title.get,
          lastName = testAcc.lastName,
          room     = testAcc.deversityDetails.get.room.get,
          status   = testAcc.deversityDetails.get.statusConfirmed
        )
      }
    }
  }
}
