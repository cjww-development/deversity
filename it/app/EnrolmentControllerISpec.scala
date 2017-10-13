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
package app

import com.cjwwdev.security.encryption.DataSecurity
import models.DeversityEnrolment
import models.formatters.MongoFormatting
import play.api.libs.json.JsSuccess
import utils.{AccountEnums, IntegrationStubbing}
import play.api.test.Helpers._

class EnrolmentControllerISpec extends IntegrationStubbing {

  implicit val formatter = DeversityEnrolment.format(MongoFormatting)

  s"/$testUserId/create-deversity-id" should {
    "return an Ok" when {
      "a deversity id has been created for a user" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.isAuthorised

        whenReady(client(s"$appUrl/$testUserId/create-deversity-id").patch("abc")) {
          _.status mustBe OK
        }

        given.user.individualUser.getUser.\("enrolments").\("deversityId").as[String].contains("deversity") mustBe true


      }
    }

    "return a Conflict" when {
      "the user already has a deversity id" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.hasDeversityId
          .user.individualUser.isAuthorised

        whenReady(client(s"$appUrl/$testUserId/create-deversity-id").patch("abc")) {
          _.status mustBe CONFLICT
        }
      }
    }

    "return a NotFound" when {
      "the user doesn't exist" in {
        given
          .user.individualUser.isAuthorised

        whenReady(client(s"$appUrl/$testUserId/create-deversity-id").patch("")) {
          _.status mustBe NOT_FOUND
        }
      }
    }
  }

  s"/enrolment/$testUserId/deversity" should {
    "return an Ok" when {
      "the deversity enrolment for the user has been found" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.isAuthorised

        whenReady(client(s"$appUrl/enrolment/$testUserId/deversity").get()) { res =>
          res.status mustBe OK
          DataSecurity.decryptIntoType[DeversityEnrolment](res.body) mustBe
            JsSuccess(testUserAccount(AccountEnums.pending, AccountEnums.teacher).deversityDetails.get)
        }
      }
    }

    "return a NotFound" when {
      "the deversity for the user isn't defined" in {
        given
          .user.individualUser.isAuthorised

        whenReady(client(s"$appUrl/enrolment/$testUserId/deversity").get()) {
          _.status mustBe NOT_FOUND
        }
      }
    }
  }
}
