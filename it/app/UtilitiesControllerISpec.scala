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

import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.security.encryption.DataSecurity
import models.formatters.MongoFormatting
import models.{OrgAccount, UserAccount}
import play.api.Logger
import play.api.libs.json.{JsSuccess, JsValue, Json, OFormat}
import utils.{AccountEnums, IntegrationTestUtils}
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class UtilitiesControllerISpec extends IntegrationTestUtils {

  s"/utilities/$testOrgId/pending-deversity-enrolments" should {
    "return an Ok" when {
      "the count has been calculated" in {
        implicit val formatOrgAcc: OFormat[OrgAccount] = OrgAccount.format(MongoFormatting)
        implicit val formatUserAcc: OFormat[UserAccount] = UserAccount.format(MongoFormatting)

        await(orgAccountRepository.collection flatMap(_.insert[OrgAccount](testOrgAccount)))
        await(userAccountRepository.collection flatMap(_.insert[UserAccount](testUserAccount(AccountEnums.pending, AccountEnums.teacher))))

        wmGet(s"/auth/get-context/$testContextId", OK, DataSecurity.encryptType[AuthContext](testOrgContext))

        val request = client(s"$appUrl/utilities/$testOrgId/pending-deversity-enrolments")
          .withHeaders(
            "appId"     -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130",
            "contextId" -> testContextId
          ).get()

        val result = await(request)
        result.status mustBe OK
        DataSecurity.decryptIntoType[JsValue](result.body) mustBe JsSuccess(Json.parse("""{"pendingCount" : 1}"""))

        afterITest()
      }
    }

    "return an Internal server error" when {
      "the given org Id cannot be matched against a held account" in {
        wmGet(s"/auth/get-context/$testContextId", OK, DataSecurity.encryptType[AuthContext](testOrgContext))

        val request = client(s"$appUrl/utilities/$testOrgId/pending-deversity-enrolments")
          .withHeaders(
            "appId"     -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130",
            "contextId" -> testContextId
          ).get()

        val result = await(request)
        result.status mustBe INTERNAL_SERVER_ERROR

        afterITest()
      }
    }
  }
}
