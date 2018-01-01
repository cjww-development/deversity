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

import javax.inject.Inject

import config.{EnrolmentsNotFoundException, Logging}
import models.OrgAccount
import repositories.{OrgAccountRepository, RegistrationCodeRepository, UserAccountRepository}
import services.selectors.OrgAccountSelectors.{orgDevIdSelector, orgIdSelector}
import services.selectors.UserAccountSelectors.teacherSelector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ValidationServiceImpl @Inject()(val userAccountRepository: UserAccountRepository,
                                      val orgAccountRepository: OrgAccountRepository,
                                      val registrationCodeRepository: RegistrationCodeRepository) extends ValidationService

trait ValidationService extends Logging {
  val userAccountRepository: UserAccountRepository
  val orgAccountRepository: OrgAccountRepository
  val registrationCodeRepository: RegistrationCodeRepository

  def validateSchool(regCode: String): Future[String] = {
    for {
      orgId  <- registrationCodeRepository.lookupUserIdByRegCode(regCode)
      school <- orgAccountRepository.getSchool(orgIdSelector(orgId))
    } yield school.deversityId
  }

  def validateTeacher(regCode: String, schoolDevId: String): Future[String] = {
    for {
      teacherUserId <- registrationCodeRepository.lookupUserIdByRegCode(regCode)
      orgAccount    <- orgAccountRepository.getSchool(orgDevIdSelector(schoolDevId))
      teacher       <- userAccountRepository.getUserBySelector(teacherSelector(teacherUserId, orgAccount.deversityId))
    } yield teacher.enrolments.fold(noTeacher(orgAccount))(_.\("deversityId").as[String])
  }

  private def noTeacher(orgAccount: OrgAccount) = {
    logger.warn(s"[validateTeacher] - There is no confirmed or pending teacher at ${orgAccount.orgName} by the given username")
    throw new EnrolmentsNotFoundException(s"There is no confirmed or pending teacher at ${orgAccount.orgName} by the given username")
  }
}
