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
import common.{AlreadyExistsException, MissingAccountException}
import helpers._
import models.DeversityEnrolment
import models.formatters.MongoFormatting
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers
import play.api.libs.json.JsSuccess
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class EnrolmentControllerSpec extends PlaySpec with MockitoSugar with GenericHelpers with ComponentMocks with Fixtures {

  val testController = new EnrolmentController(mockAuthConnector, mockConfig, mockEnrolmentService)

  val testUserId = generateTestSystemId(USER)
  val testDevId  = generateTestSystemId(DEVERSITY)

  implicit val enrolmentFormatter = DeversityEnrolment.format(MongoFormatting)

  "createDeversityId" should {
    "return an Ok" when {
      "a deversity id has been created for the user" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        ).withBody("")

        when(mockEnrolmentService.createDeversityId(ArgumentMatchers.any()))
          .thenReturn(Future.successful(DataSecurity.encryptString(testDevId)))

        val result = testController.createDeversityId(testUserId)
        AuthBuilder.postWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result => status(result) mustBe OK
        }
      }
    }

    "return a Conflict" when {
      "when a user exists but the user already has an deversity id" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        ).withBody("")

        when(mockEnrolmentService.createDeversityId(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new AlreadyExistsException("")))

        val result = testController.createDeversityId(testUserId)
        AuthBuilder.postWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result => status(result) mustBe CONFLICT
        }
      }
    }

    "return a NotFound" when {
      "the user doesn't exist" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        ).withBody("")

        when(mockEnrolmentService.createDeversityId(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new MissingAccountException("")))

        val result = testController.createDeversityId(testUserId)
        AuthBuilder.postWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result => status(result) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was some other problem" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        ).withBody("")

        when(mockEnrolmentService.createDeversityId(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new IllegalStateException("")))

        val result = testController.createDeversityId(testUserId)
        AuthBuilder.postWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result => status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getDeversityEnrolment" should {
    "return an Ok" when {
      "the users enrolment has been fetched" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        )

        when(mockEnrolmentService.getEnrolment(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Some(testStudentEnrolment(AccountEnums.pending))))

        val result = testController.getDeversityEnrolment(testUserId)
        AuthBuilder.getWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result =>
            status(result) mustBe OK
            DataSecurity.decryptIntoType[DeversityEnrolment](contentAsString(result)) mustBe JsSuccess(testStudentEnrolment(AccountEnums.pending))
        }
      }
    }

    "return a NotFound" when {
      "a user can't be found against the given id" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        )

        when(mockEnrolmentService.getEnrolment(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new MissingAccountException("")))

        val result = testController.getDeversityEnrolment(testUserId)
        AuthBuilder.getWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result => status(result) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was some other problem" in {
        val request  = FakeRequest().withHeaders(
          "appId" -> AUTH_SERVICE_ID,
          CONTENT_TYPE -> TEXT
        )

        when(mockEnrolmentService.getEnrolment(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new IllegalStateException("")))

        val result = testController.getDeversityEnrolment(testUserId)
        AuthBuilder.getWithAuthorisedUser(result, request, mockAuthConnector, uuid, "user") {
          result => status(result) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
