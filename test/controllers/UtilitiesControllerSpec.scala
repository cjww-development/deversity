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
package controllers

import com.cjwwdev.auth.models.CurrentUser
import common.MissingAccountException
import helpers.controllers.ControllerSpec
import models.{OrgDetails, TeacherDetails}
import models.formatters.{APIFormatting, MongoFormatting}
import play.api.mvc.{Request, Result}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class UtilitiesControllerSpec extends ControllerSpec {

  val testController = new UtilitiesController {
    override val utilitiesService    = mockUtilitiesService
    override val authConnector       = mockAuthConnector

    override protected def authorised(userId: String)(f: (CurrentUser) => Future[Result])(implicit request: Request[_]) = {
      f(testOrgCurrentUser)
    }
  }

  "getPendingEnrolmentsCount" should {
    "return an Ok" when {
      "the count has been calculated" in {
        mockGetPendingEnrolments(returned = Future(1))

        assertResult(testController.getPendingEnrolmentsCount(generateTestSystemId(ORG))(standardRequest)) {
          status(_) mustBe OK
        }
      }
    }

    "return an Internal server error" when {
      "no org account matching the given orgId was found" in {
        mockGetPendingEnrolments(returned = Future.failed(new MissingAccountException("")))

        assertResult(testController.getPendingEnrolmentsCount(generateTestSystemId(ORG))(standardRequest)) {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getSchoolDetails" should {
    "return an OK" when {
      "school details have been found and encrypted" in {
        mockGetSchoolDetails(fetched = true)

        assertResult(testController.getSchoolDetails(testUserId, testOrgDevId.encrypt)(standardRequest)) { res =>
          status(res) mustBe OK
          contentAsString(res).decryptType(OrgDetails.reads(MongoFormatting)) mustBe testOrgDetails
        }
      }
    }

    "return a NotFound" when {
      "no school details could be found" in {
        mockGetSchoolDetails(fetched = false)

        assertResult(testController.getSchoolDetails(testUserId, testOrgDevId.encrypt)(standardRequest)) { res =>
          status(res) mustBe NOT_FOUND
        }
      }
    }
  }

  "getTeacherDetails" should {
    "return an OK" when {
      "a teachers details has been found and encrypted" in {
        mockGetTeacherDetails(fetched = true)

        assertResult(testController.getTeacherDetails(testUserId, testDeversityId.encrypt, testOrgDevId.encrypt)(standardRequest)) { res =>
          status(res) mustBe OK
          contentAsString(res).decryptType(TeacherDetails.reads(MongoFormatting)) mustBe testTeacherDetails
        }
      }
    }

    "return a NotFound" when {
      "no teacher details could be found" in {
        mockGetTeacherDetails(fetched = false)

        assertResult(testController.getTeacherDetails(testUserId, testDeversityId.encrypt, testOrgDevId.encrypt)(standardRequest)) { res =>
          status(res) mustBe NOT_FOUND
        }
      }
    }
  }
}
