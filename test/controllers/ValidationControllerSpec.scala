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
package controllers

import com.cjwwdev.security.encryption.DataSecurity
import helpers.{ComponentMocks, Fixtures, GenericHelpers}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class ValidationControllerSpec extends PlaySpec with MockitoSugar with GenericHelpers with ComponentMocks with Fixtures {

  val testController = new ValidationController(mockConfig, mockValidationService)

  val encryptedSchoolName = DataSecurity.encryptString("tSchoolName")
  val encryptedUserName = DataSecurity.encryptString(createTestUserName)

  "validateSchool" should {
    "return an ok" when {
      "the school has been successfully validated" in {
        when(mockValidationService.validateSchool(ArgumentMatchers.any()))
          .thenReturn(Future.successful(true))

        val request = FakeRequest().withHeaders("appID" -> mockConfig.getApplicationId("auth-service"))

        val result = testController.validateSchool(encryptedSchoolName)(request)
        status(result) mustBe OK
      }
    }

    "return a Not found" when {
      "the school has not been validated" in {
        when(mockValidationService.validateSchool(ArgumentMatchers.any()))
          .thenReturn(Future.successful(false))

        val request = FakeRequest().withHeaders("appID" -> mockConfig.getApplicationId("auth-service"))

        val result = testController.validateSchool(encryptedSchoolName)(request)
        status(result) mustBe NOT_FOUND
      }
    }
  }

  "validateTeacher" should {
    "return an ok" when {
      "the teacher has been successfully validated" in {
        when(mockValidationService.validateTeacher(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(true))

        val request = FakeRequest().withHeaders("appID" -> mockConfig.getApplicationId("auth-service"))

        val result = testController.validateTeacher(encryptedUserName, encryptedSchoolName)(request)
        status(result) mustBe OK
      }
    }

    "return a Not found" when {
      "the teacher has not been validated" in {
        when(mockValidationService.validateTeacher(ArgumentMatchers.any(), ArgumentMatchers.any()))
          .thenReturn(Future.successful(false))

        val request = FakeRequest().withHeaders("appID" -> mockConfig.getApplicationId("auth-service"))

        val result = testController.validateTeacher(encryptedUserName, encryptedSchoolName)(request)
        status(result) mustBe NOT_FOUND
      }
    }
  }
}
