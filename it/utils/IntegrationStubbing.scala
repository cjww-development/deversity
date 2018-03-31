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
package utils

import com.cjwwdev.auth.models.CurrentUser
import com.cjwwdev.security.encryption.DataSecurity
import models.formatters.MongoFormatting
import models.{ClassRoom, OrgAccount, UserAccount}
import play.api.libs.json.{JsValue, OFormat}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global

trait IntegrationStubbing {
  self: IntegrationSpec =>

  implicit val formatOrgAcc: OFormat[OrgAccount]   = OrgAccount.format(MongoFormatting)
  implicit val formatUserAcc: OFormat[UserAccount] = UserAccount.format(MongoFormatting)

  val testUserAcc: UserAccount = testUserAccount(AccountEnums.teacher)

  val testClassId = generateTestSystemId("class")

  class PreconditionBuilder {
    implicit val builder: PreconditionBuilder = this

    def user: UserStub = UserStub()
  }

  def given: PreconditionBuilder = new PreconditionBuilder

  case class UserStub()(implicit builder: PreconditionBuilder) {
    def individualUser: IndividualUser = IndividualUser()
    def orgUser: OrgUser = OrgUser()
  }

  case class IndividualUser()(implicit builder: PreconditionBuilder) {
    def isSetup: PreconditionBuilder = {
      await(userAccountRepository.collection flatMap(_.insert[UserAccount](testUserAcc)))
      builder
    }

    def hasDeversityId: PreconditionBuilder = {
      await(userAccountRepository.collection flatMap {
        _.update(BSONDocument("userName" -> "tUserName"), BSONDocument("$set" -> BSONDocument("enrolments.deversityId" -> testDeversityId)))
      })
      builder
    }

    def hasRegistrationCode(userId: String, regCode: String): PreconditionBuilder = {
      await(regCodeRepository.getRegistrationCode(userId, regCode))
      builder
    }

    def hasClasses: PreconditionBuilder = {
      await(classRoomRepository.collection.flatMap(_.insert[ClassRoom](ClassRoom(testClassId, testDeversityId, testDeversityId, "Test class name"))))
      builder
    }

    def isAuthorised: PreconditionBuilder = {
      stubbedGet(s"/session-store/session/$testCookieId/context", OK, testContextId.encrypt)
      stubbedGet(s"/auth/get-current-user/${generateTestSystemId(CONTEXT)}", OK, DataSecurity.encryptType[CurrentUser](testCurrentUser))
      builder
    }

    def getUser: JsValue = {
      await(userAccountRepository.collection flatMap {
        _.find(BSONDocument("userId" -> testUserId)).one[JsValue]
      }).get
    }
  }

  case class OrgUser()(implicit builder: PreconditionBuilder) {
    def isSetup: PreconditionBuilder = {
      await(orgAccountRepository.collection flatMap(_.insert[OrgAccount](testOrgAccount)))
      builder
    }

    def hasRegistrationCode(userId: String, regCode: String): PreconditionBuilder = {
      await(regCodeRepository.getRegistrationCode(userId, regCode))
      builder
    }

    def isAuthorised: PreconditionBuilder = {
      stubbedGet(s"/session-store/session/$testCookieId/context", OK, testContextId.encrypt)
      stubbedGet(s"/auth/get-current-user/${generateTestSystemId(CONTEXT)}", OK, DataSecurity.encryptType[CurrentUser](testOrgCurrentUser))
      builder
    }
  }
}

