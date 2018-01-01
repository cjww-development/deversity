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
import config.{MissingAccountException, RegistrationCodeNotFoundException}
import helpers.{AccountEnums, ComponentMocks, Fixtures}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class ValidationServiceSpec extends CJWWSpec with MockitoSugar with ComponentMocks with Fixtures {

  val testService = new ValidationService {
    override val userAccountRepository      = mockUserAccountRepo
    override val orgAccountRepository       = mockOrgAccountRepo
    override val registrationCodeRepository = mockRegCodeRepository
  }

  "validateSchool" should {
    "return true" when {
      "a school has been successfully validated" in {
        val testDevId = generateTestSystemId(ORG)

        when(mockRegCodeRepository.lookupUserIdByRegCode(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testDevId))

        when(mockOrgAccountRepo.getSchool(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testOrgAccount))

        val result = await(testService.validateSchool("tSchoolName"))
        result mustBe testOrgAccount.deversityId
      }
    }

    "return false" when {
      "a school has not been validated" in {
        when(mockRegCodeRepository.lookupUserIdByRegCode(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new RegistrationCodeNotFoundException("")))

        intercept[RegistrationCodeNotFoundException](await(testService.validateSchool("tSchoolName")))
      }
    }
  }

  "validateTeacher" ignore {
    "return true" when {
      "a teacher has been successfully validated" in {
        val testUserId     = generateTestSystemId(USER)
        val testTeacherAcc = testUserAccount(AccountEnums.pending, AccountEnums.teacher)
        val testRegCode    = "testRegCode"

        when(mockRegCodeRepository.lookupUserIdByRegCode(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testOrgAccount.deversityId))

        when(mockOrgAccountRepo.getSchool(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testOrgAccount))

        when(mockUserAccountRepo.getUserBySelector(ArgumentMatchers.any()))
          .thenReturn(Future.successful(testTeacherAcc))

        val result = await(testService.validateTeacher(testRegCode, testOrgDevId))
        result mustBe true
      }
    }

    "return false" when {
      "a teacher has not been validated" in {
        when(mockUserAccountRepo.getUserBySelector(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new MissingAccountException("")))

        val result = await(testService.validateTeacher(createTestUserName, testOrgDevId))
        result mustBe false
      }
    }
  }
}
