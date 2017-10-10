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
package utils

import com.cjwwdev.auth.models.AuthContext
import com.cjwwdev.security.encryption.DataSecurity
import models.{OrgAccount, UserAccount}
import models.formatters.MongoFormatting
import play.api.libs.json.{JsValue, OFormat}
import play.api.test.Helpers.OK
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait IntegrationStubbing extends IntegrationTestUtils {

  implicit val formatOrgAcc: OFormat[OrgAccount] = OrgAccount.format(MongoFormatting)
  implicit val formatUserAcc: OFormat[UserAccount] = UserAccount.format(MongoFormatting)

  class PreconditionBuilder {
    implicit val builder: PreconditionBuilder = this

    def user: UserStub = UserStub()

    def result: Result = Result()
  }

  def given: PreconditionBuilder = new PreconditionBuilder

  case class UserStub()(implicit builder: PreconditionBuilder) {
    def individualUser: IndividualUser = IndividualUser()
    def orgUser: OrgUser = OrgUser()
  }

  case class IndividualUser()(implicit builder: PreconditionBuilder) {
    def isSetup: PreconditionBuilder = {
      await(userAccountRepository.collection flatMap {
        _.insert[UserAccount](testUserAccount(AccountEnums.pending, AccountEnums.teacher))
      })
      builder
    }

    def hasDeversityId: PreconditionBuilder = {
      await(userAccountRepository.collection flatMap {
        _.update(BSONDocument(), BSONDocument("$set" -> BSONDocument("enrolments.deversityId" -> testDeversityId)))
      })
      builder
    }

    def isAuthorised: PreconditionBuilder = {
      wmGet(s"/auth/get-context/$testContextId", OK, DataSecurity.encryptType[AuthContext](testUserContext))
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

    def isAuthorised: PreconditionBuilder = {
      wmGet(s"/auth/get-context/$testContextId", OK, DataSecurity.encryptType[AuthContext](testOrgContext))
      builder
    }
  }

  case class Result() {
    def execute[T](action: Future[T])(f: T => Any): Any = f(await(action))
  }
}

