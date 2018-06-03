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
import com.cjwwdev.implicits.ImplicitDataSecurity._
import helpers.controllers.ControllerSpec
import models.formatters.MongoFormatting
import models.{OrgDetails, TeacherDetails}
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

class UtilitiesControllerSpec extends ControllerSpec {

  val testController = new UtilitiesController {
    override protected def controllerComponents = stubControllerComponents()
    override val utilitiesService               = mockUtilitiesService
    override val authConnector                  = mockAuthConnector

    override protected def authorised(userId: String)(f: (CurrentUser) => Future[Result])(implicit request: Request[_]) = {
      f(testOrgCurrentUser)
    }
  }

  "getSchoolDetails" should {
    "return an OK" when {
      "school details have been found and encrypted" in {
        mockGetSchoolDetails(fetched = true)

        assertResult(testController.getSchoolDetails(testUserId, testOrgDevId.encrypt)(standardRequest)) { res =>
          status(res) mustBe OK
          contentAsJson(res).\("body").as[String].decryptIntoType(OrgDetails.reads(MongoFormatting)) mustBe testOrgDetails
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
          contentAsJson(res).\("body").as[String].decryptIntoType(TeacherDetails.reads(MongoFormatting)) mustBe testTeacherDetails
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
