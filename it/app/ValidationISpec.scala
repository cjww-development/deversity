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
import models.formatters.MongoFormatting
import models.{OrgAccount, UserAccount}
import play.api.libs.json.OFormat
import utils.{AccountEnums, IntegrationTestUtils}
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationISpec extends IntegrationTestUtils {

  val encryptedSchoolName = DataSecurity.encryptString(testOrgAccount.orgUserName)
  val encryptedUserName = DataSecurity.encryptString("tUserName")

  s"/validate/school/:schoolName" should {
    "return an OK" when {
      "the school has been validated" in {

        implicit val format: OFormat[OrgAccount] = OrgAccount.format(MongoFormatting)

        await(orgAccountRepository.collection.flatMap(_.insert[OrgAccount](testOrgAccount)))

        val request = client(s"$appUrl/validate/school/$encryptedSchoolName")
          .withHeaders("appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130")
          .head()

        val result = await(request)
        result.status mustBe OK

        afterITest()
      }
    }

    "return a Not found" when {
      "the school has not been validated" in {
        val request = client(s"$appUrl/validate/school/$encryptedSchoolName")
          .withHeaders("appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130")
          .head()

        val result = await(request)
        result.status mustBe NOT_FOUND
      }
    }
  }

  "/validate/teacher/:userName/school/:schoolName" should {
    "return an OK" when {
      "the teacher has been validated" in {

        implicit val format: OFormat[UserAccount] = UserAccount.format(MongoFormatting)

        await(userAccountRepository.collection.flatMap(_.insert[UserAccount](testUserAccount(AccountEnums.pending, AccountEnums.teacher))))

        val request = client(s"$appUrl/validate/teacher/$encryptedUserName/school/$encryptedSchoolName")
          .withHeaders("appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130")
          .head()

        val result = await(request)
        result.status mustBe OK

        afterITest()
      }
    }

    "return a Not found" when {
      "the teacher has not been validated" in {
        val request = client(s"$appUrl/validate/teacher/$encryptedUserName/school/$encryptedSchoolName")
          .withHeaders("appId" -> "abda73f4-9d52-4bb8-b20d-b5fffd0cc130")
          .head()

        val result = await(request)
        result.status mustBe NOT_FOUND
      }
    }
  }
}
