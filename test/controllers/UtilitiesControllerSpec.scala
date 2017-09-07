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

import com.cjwwdev.auth.models.AuthContext
import common.MissingAccountException
import helpers.{ComponentMocks, Fixtures, GenericHelpers}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class UtilitiesControllerSpec extends PlaySpec with MockitoSugar with GenericHelpers with ComponentMocks with Fixtures  {

  val testController = new UtilitiesController(mockUtilitiesService, mockConfig, mockAuthConnector) {
    override protected def authorised(userId: String)(f: (AuthContext) => Future[Result])(implicit request: Request[_]) = {
      f(testOrgContext)
    }
  }

  "getPendingEnrolmentsCount" should {
    "return an Ok" when {
      "the count has been calculated" in {
        when(mockUtilitiesService.getPendingEnrolmentCount(ArgumentMatchers.any()))
          .thenReturn(Future.successful(Json.parse("""{"pendingCount" : 1}""")))

        val request = FakeRequest().withHeaders("appID" -> mockConfig.getApplicationId("auth-service"))

        val result = testController.getPendingEnrolmentsCount(generateTestSystemId(ORG))(request)
        status(result) mustBe OK
      }
    }

    "return an Internal server error" when {
      "no org account matching the given orgId was found" in {
        when(mockUtilitiesService.getPendingEnrolmentCount(ArgumentMatchers.any()))
          .thenReturn(Future.failed(new MissingAccountException("")))

        val request = FakeRequest().withHeaders("appID" -> mockConfig.getApplicationId("auth-service"))

        val result = testController.getPendingEnrolmentsCount(generateTestSystemId(ORG))(request)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
