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
package services

import com.cjwwdev.logging.Logging
import javax.inject.Inject
import models.{OrgAccount, OrgDetails, TeacherDetails, UserAccount}
import repositories.{OrgAccountRepository, UserAccountRepository}
import selectors.OrgAccountSelectors.orgDevIdSelector
import selectors.UserAccountSelectors.teacherDetailsSelector

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultUtilitiesService @Inject()(val userAccountRepository: UserAccountRepository,
                                        val orgAccountRepository: OrgAccountRepository) extends UtilitiesService

trait UtilitiesService extends Logging {
  val userAccountRepository: UserAccountRepository
  val orgAccountRepository: OrgAccountRepository

  def getSchoolDetails(orgUserName: String)(implicit ec: ExC): Future[OrgDetails] = {
    orgAccountRepository.getSchool(orgDevIdSelector(orgUserName)) map accountToDetails
  }

  def getTeacherDetails(userName: String, schoolName: String)(implicit ec: ExC): Future[TeacherDetails] = {
    userAccountRepository.getUserBySelector(teacherDetailsSelector(userName, schoolName)) map accountToTeacherDetails
  }

  private def accountToDetails(orgAccount: OrgAccount): OrgDetails = OrgDetails(
    orgName  = orgAccount.orgName,
    initials = orgAccount.initials,
    location = orgAccount.location
  )

  private def accountToTeacherDetails(userAccount: UserAccount): TeacherDetails = TeacherDetails(
    userId    = userAccount.userId,
    title     = userAccount.deversityDetails.get.title.get,
    lastName  = userAccount.lastName,
    room      = userAccount.deversityDetails.get.room.get
  )
}
