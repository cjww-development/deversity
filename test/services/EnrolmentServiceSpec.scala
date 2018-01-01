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

import com.cjwwdev.reactivemongo.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import com.cjwwdev.test.CJWWSpec
import helpers.{AccountEnums, ComponentMocks, Fixtures}
import models.RegistrationCode
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import com.cjwwdev.test.mongo.FakeMongoResults

import scala.concurrent.Future

class EnrolmentServiceSpec extends CJWWSpec with ComponentMocks with Fixtures {

  val testService = new EnrolmentService {
    override val userAccountRepository = mockUserAccountRepo
    override val orgAccountRepository = mockOrgAccountRepo
    override val registrationCodeRepository = mockRegCodeRepository
  }

  val testUserId = generateTestSystemId(USER)

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
      result mustBe testAcc.deversityDetails
    }
  }

  "updateDeversityEnrolment" should {
    val testDeversityEnrolment = testTeacherEnrolment(AccountEnums.pending)

    "return a MongoSuccessUpdate" in {
      when(mockUserAccountRepo.updateDeversityEnrolment(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(MongoSuccessUpdate))

      val result = await(testService.updateDeversityEnrolment(testUserId, testDeversityEnrolment))
      result mustBe MongoSuccessUpdate
    }

    "return an MongoFailedUpdate" in {
      when(mockUserAccountRepo.updateDeversityEnrolment(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(MongoFailedUpdate))

      val result = await(testService.updateDeversityEnrolment(testUserId, testDeversityEnrolment))
      result mustBe MongoFailedUpdate
    }
  }

  "getRegistrationCode" should {
    val testRegCode = RegistrationCode(testUserId, "testRegCode", DateTime.now)

    "return a registration code" in {
      when(mockRegCodeRepository.getRegistrationCode(ArgumentMatchers.any(), ArgumentMatchers.any()))
        .thenReturn(Future.successful(testRegCode))

      val result = await(testService.getRegistrationCode(testUserId))
      result mustBe testRegCode
    }
  }

//  "generateRegistrationCode" should {
//    "return true" when {
//      "the generation of the reg code was successful" in {
//        when(mockRegCodeRepository.generateRegistrationCode(ArgumentMatchers.any(), ArgumentMatchers.any()))
//          .thenReturn(Future.successful(fakeSuccessUpdateWR))
//
//        val result = await(testService.generateRegistrationCode(testUserId))
//        assert(result)
//      }
//    }
//
//    "return false" when {
//      "there was a problem generating a reg code" in {
//        when(mockRegCodeRepository.generateRegistrationCode(ArgumentMatchers.any(), ArgumentMatchers.any()))
//          .thenReturn(Future.successful(fakeFailedUpdateWR))
//
//        val result = await(testService.generateRegistrationCode(testUserId))
//        assert(!result)
//      }
//    }
//  }
}
