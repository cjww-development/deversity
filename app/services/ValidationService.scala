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

import play.api.Logger
import repositories.{OrgAccountRepository, UserAccountRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ValidationService @Inject()(userAccountRepository: UserAccountRepository, orgAccountRepository: OrgAccountRepository) {

  def validateSchool(schoolName: String): Future[Boolean] = {
    orgAccountRepository.getSchoolByUserName(schoolName) map {
      case Some(_) => true
      case None    =>
        Logger.warn(s"[ValidationService] - [validateSchool] - There is no registered school by the name $schoolName")
        false
    }
  }

  def validateTeacher(userName: String, schoolName: String): Future[Boolean] = {
    userAccountRepository.getTeacher(userName, schoolName) map {
      case Some(_) => true
      case None    =>
        Logger.warn(s"[ValidationService] - [validateTeacher] - There is no confirmed or pending teacher at $schoolName by the given username")
        false
    }
  }
}
