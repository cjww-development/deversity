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
package services

import com.cjwwdev.test.CJWWSpec
import config.MissingAccountException
import helpers.{AccountEnums, ComponentMocks, Fixtures}
import models.TeacherDetails
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.Json

import scala.concurrent.Future

class UtilitiesServiceSpec extends CJWWSpec with ComponentMocks with Fixtures {

  val testService = new UtilitiesService {
    override val userAccountRepository = mockUserAccountRepo
    override val orgAccountRepository = mockOrgAccountRepo
  }

  "getPendingEnrolmentCount" should {
    "return a JsValue" when {
      "the count has been calculated" in {
        when(mockOrgAccountRepo.getSchool(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testOrgAccount))

        when(mockUserAccountRepo.getPendingEnrolmentCount(ArgumentMatchers.any()))
          .thenReturn(Future.successful(1))

        val result = await(testService.getPendingEnrolmentCount(generateTestSystemId(ORG)))
        result mustBe Json.parse("""{"pendingCount" : 1}""")
      }
    }

    "throw a missing account exception" when {
      "the given orgId doesn't matched a held account" in {
        when(mockOrgAccountRepo.getSchool(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new MissingAccountException("")))

        intercept[MissingAccountException](await(testService.getPendingEnrolmentCount(generateTestSystemId(ORG))))
      }
    }
  }

  "getSchoolDetails" should {
    "return an org details" in {
      when(mockOrgAccountRepo.getSchool(ArgumentMatchers.any()))
        .thenReturn(Future.successful(testOrgAccount))

      val result = await(testService.getSchoolDetails("testOrgName"))
      result mustBe testOrgDetails
    }
  }

  "getTeacherDetails" should {
    "return a teacher details model" in {
      val testAcc = testUserAccount(AccountEnums.confirmed, AccountEnums.teacher)

      when(mockUserAccountRepo.getUserBySelector(ArgumentMatchers.any()))
        .thenReturn(Future.successful(testAcc))

      val result = await(testService.getTeacherDetails("testUserName", "testSchoolName"))
      result mustBe TeacherDetails(
        userId   = testAcc.userId,
        title    = testAcc.deversityDetails.get.title.get,
        lastName = testAcc.lastName,
        room     = testAcc.deversityDetails.get.room.get,
        status   = testAcc.deversityDetails.get.statusConfirmed
      )
    }
  }
}
