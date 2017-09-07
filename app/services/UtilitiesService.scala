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
package services

import javax.inject.{Inject, Singleton}

import common.MissingAccountException
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import repositories.{OrgAccountRepository, UserAccountRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UtilitiesService @Inject()(userAccountRepository: UserAccountRepository, orgAccountRepository: OrgAccountRepository) {
  def getPendingEnrolmentCount(orgId: String): Future[JsValue] = {
    for {
      orgAcc <- orgAccountRepository.getSchoolById(orgId) map {
        case Some(acc) => acc
        case None      => throw new MissingAccountException(s"No org account found for org id $orgId")
      }
      count  <- userAccountRepository.getPendingEnrolmentCount(orgAcc.orgUserName)
    } yield {
      Logger.info(s"[UtilitiesService] - [getPendingEnrolmentCount] - Got pending enrolment count for org id $orgId")
      Json.parse(s"""{"pendingCount" : $count}""")
    }
  }
}
