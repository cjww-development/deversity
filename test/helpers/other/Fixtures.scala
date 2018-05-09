/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package helpers.other

import com.cjwwdev.auth.models.CurrentUser
import models._
import play.api.libs.json.Json

trait Fixtures extends TestDataGenerator {
  val testOrgDevId = generateTestSystemId(DEVERSITY)

  val testOrgCurrentUser = CurrentUser(
    contextId       = generateTestSystemId(CONTEXT),
    id              = generateTestSystemId(ORG),
    orgDeversityId  = Some(generateTestSystemId(DEVERSITY)),
    credentialType  = "organisation",
    orgName         = None,
    firstName       = None,
    lastName        = None,
    role            = None,
    enrolments      = None
  )

  val testCurrentUser = CurrentUser(
    contextId       = generateTestSystemId(CONTEXT),
    id              = generateTestSystemId(USER),
    orgDeversityId  = Some(generateTestSystemId(DEVERSITY)),
    credentialType  = "individual",
    orgName         = None,
    firstName       = Some("testFirstName"),
    lastName        = Some("testLastName"),
    role            = None,
    enrolments      = Some(Json.obj(
      "deversityId" -> generateTestSystemId(DEVERSITY)
    ))
  )

  def testTeacherEnrolment: DeversityEnrolment = {
    DeversityEnrolment(
      schoolDevId = testOrgDevId,
      role        = "teacher",
      title       = Some("testTitle"),
      room        = Some("testRoom"),
      teacher     = None
    )
  }

  def testStudentEnrolment: DeversityEnrolment = {
    DeversityEnrolment(
      schoolDevId = testOrgDevId,
      role        = "student",
      title       = None,
      room        = None,
      teacher     = Some(createTestUserName)
    )
  }

  def testUserAccount(accountType: AccountEnums.Value): UserAccount = {
    val accType = if(accountType == AccountEnums.teacher) {
      Some(testTeacherEnrolment)
    } else if(accountType == AccountEnums.student) {
      Some(testStudentEnrolment)
    } else {
      None
    }

    val enrs    = if(accountType == AccountEnums.teacher | accountType == AccountEnums.student) {
      Some(Json.obj("deversityId" -> generateTestSystemId(DEVERSITY)))
    } else {
      None
    }

    UserAccount(
      userId           = generateTestSystemId(USER),
      firstName        = "testFirstName",
      lastName         = "testLastName",
      userName         = createTestUserName,
      email            = createTestEmail,
      deversityDetails = accType,
      enrolments       = enrs
    )
  }

  val testOrgAccount = OrgAccount(
    orgId       = generateTestSystemId(ORG),
    deversityId = testOrgDevId,
    orgName     = "testSchoolName",
    initials    = "TSN",
    orgUserName = "tSchoolName",
    location    = "testLocation",
    orgEmail    = createTestEmail
  )

  val testOrgDetails = OrgDetails(
    orgName  = testOrgAccount.orgName,
    initials = testOrgAccount.initials,
    location = testOrgAccount.location
  )

  val testClassRoom = ClassRoom(
    classId       = generateTestSystemId("class"),
    schooldevId   = generateTestSystemId(DEVERSITY),
    teacherDevId  = generateTestSystemId(DEVERSITY),
    name          = "testClass1"
  )

  val testClassList = List(
    ClassRoom(generateTestSystemId("class"), generateTestSystemId(DEVERSITY), generateTestSystemId(DEVERSITY), "testClass1"),
    ClassRoom(generateTestSystemId("class"), generateTestSystemId(DEVERSITY), generateTestSystemId(DEVERSITY), "testClass2"),
    ClassRoom(generateTestSystemId("class"), generateTestSystemId(DEVERSITY), generateTestSystemId(DEVERSITY), "testClass3")
  )

  val testTeacherDetails = TeacherDetails(
    userId   = generateTestSystemId(USER),
    title    = "testTitle",
    lastName = "testLastName",
    room     = "testRoom"
  )

  val testRegistrationCode = RegistrationCode(
    identifier = generateTestSystemId(USER),
    code       = "testRegCode",
    createdAt  = now
  )
}
