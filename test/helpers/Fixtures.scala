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
package helpers

import com.cjwwdev.auth.models.{AuthContext, User}
import models.{DeversityEnrolment, OrgAccount, UserAccount}

trait Fixtures extends TestDataHelper {

  val testOrgContext: AuthContext = AuthContext(
    contextId = generateTestSystemId(CONTEXT),
    user = User(
      userId         = generateTestSystemId(USER),
      firstName      = None,
      lastName       = None,
      orgName        = Some("testOrgName"),
      credentialType = "organisation",
      role           = None
    ),
    basicDetailsUri = "/test/uri",
    enrolmentsUri   = "/test/uri",
    settingsUri     = "/test/uri",
    createdAt       = now
  )

  def testTeacherEnrolment(status: AccountEnums.Value): DeversityEnrolment = {
    val stat = if(status == AccountEnums.confirmed) AccountEnums.confirmed.toString else AccountEnums.pending.toString

    DeversityEnrolment(
      statusConfirmed = stat,
      schoolName      = "tSchoolName",
      role            = "teacher",
      title           = Some("testTitle"),
      room            = Some("testRoom"),
      teacher         = None
    )
  }

  def testStudentEnrolment(status: AccountEnums.Value): DeversityEnrolment = {
    val stat = if(status == AccountEnums.confirmed) AccountEnums.confirmed.toString else AccountEnums.pending.toString

    DeversityEnrolment(
      statusConfirmed = stat,
      schoolName      = "tSchoolName",
      role            = "student",
      title           = None,
      room            = None,
      teacher         = Some(createTestUserName)
    )
  }

  def testUserAccount(status: AccountEnums.Value, accountType: AccountEnums.Value): UserAccount = {
    val accType = if(accountType == AccountEnums.teacher) testTeacherEnrolment(status) else testStudentEnrolment(status)

    UserAccount(
      userId    = generateTestSystemId(USER),
      firstName = "testFirstName",
      lastName  = "testLastName",
      userName  = createTestUserName,
      email     = createTestEmail,
      deversityEnrolment = accType
    )
  }

  val testOrgAccount = OrgAccount(
    orgId       = generateTestSystemId(ORG),
    orgName     = "testSchoolName",
    initials    = "TSN",
    orgUserName = "tSchoolName",
    location    = "testLocation",
    orgEmail    = createTestEmail
  )
}
