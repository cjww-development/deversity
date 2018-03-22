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
import models.{DeversityEnrolment, OrgAccount, UserAccount}
import play.api.libs.json.Json

trait Fixtures {
  self: TestDataGenerator =>

  object AccountEnums extends Enumeration {
    val basic     = Value
    val teacher   = Value
    val student   = Value
  }

  val testContextId   = generateTestSystemId(CONTEXT)
  val testOrgId       = generateTestSystemId(ORG)
  val testUserId      = generateTestSystemId(USER)
  val testDeversityId = generateTestSystemId(DEVERSITY)

  val testOrgCurrentUser = CurrentUser(
    contextId = generateTestSystemId(CONTEXT),
    id = generateTestSystemId(ORG),
    orgDeversityId = Some(generateTestSystemId(DEVERSITY)),
    credentialType = "organisation",
    orgName = None,
    role = None,
    enrolments = None
  )

  val testCurrentUser = CurrentUser(
    contextId = generateTestSystemId(CONTEXT),
    id = generateTestSystemId(USER),
    orgDeversityId = Some(generateTestSystemId(DEVERSITY)),
    credentialType = "individual",
    orgName = None,
    role = None,
    enrolments = Some(Json.obj(
      "deversityId" -> generateTestSystemId(DEVERSITY)
    ))
  )

  def testTeacherEnrolment: DeversityEnrolment = {
    DeversityEnrolment(
      statusConfirmed = "pending",
      schoolName      = testOrgAccount.deversityId,
      role            = "teacher",
      title           = Some("testTitle"),
      room            = Some("testRoom"),
      teacher         = None
    )
  }

  def testStudentEnrolment: DeversityEnrolment = {
    DeversityEnrolment(
      statusConfirmed = "pending",
      schoolName      = "tSchoolName",
      role            = "student",
      title           = None,
      room            = None,
      teacher         = Some(createTestUserName)
    )
  }

  def testUserAccount(accountType: AccountEnums.Value): UserAccount = {
    val accType = if(accountType == AccountEnums.teacher) testTeacherEnrolment else testStudentEnrolment

    UserAccount(
      userId    = testUserId,
      firstName = "testFirstName",
      lastName  = "testLastName",
      userName  = "tUserName",
      email     = "foo@bar.com",
      deversityDetails = Some(accType),
      enrolments = None
    )
  }

  lazy val testOrgAccount = OrgAccount(
    orgId       = testOrgId,
    deversityId = testDeversityId,
    orgName     = "tSchoolName",
    initials    = "TSN",
    orgUserName = "tSchoolName",
    location    = "testLocation",
    orgEmail    = "foo@bar.com"
  )
}
