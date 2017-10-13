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

import com.cjwwdev.security.encryption.DataSecurity
import helpers.{AccountEnums, ComponentMocks, Fixtures, GenericHelpers}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers

import scala.concurrent.Future

class EnrolmentServiceSpec extends PlaySpec with MockitoSugar with GenericHelpers with ComponentMocks with Fixtures {

  val testService = new EnrolmentService(mockUserAccountRepo)

  "createDeversityId" should {
    "return a deversity id" in {
      val testDevId = s"deversity-$uuid"

      when(mockUserAccountRepo.createDeversityId(ArgumentMatchers.any()))
        .thenReturn(Future.successful(testDevId))

      val result = await(testService.createDeversityId(generateTestSystemId(USER)))
      result mustBe DataSecurity.encryptString(testDevId)
    }
  }

  "getEnrolment" should {
    "return a DeversityEnrolment" in {

      val testAcc = testUserAccount(AccountEnums.pending, AccountEnums.student)

      when(mockUserAccountRepo.getUserBySelector(ArgumentMatchers.any()))
        .thenReturn(Future.successful(testAcc))

      val result = await(testService.getEnrolment(generateTestSystemId(USER)))
      result mustBe testAcc.deversityEnrolment
    }
  }
}
