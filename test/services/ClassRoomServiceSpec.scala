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

import com.cjwwdev.mongo.responses.{MongoFailedDelete, MongoSuccessCreate, MongoSuccessDelete}
import common.EnrolmentsNotFoundException
import helpers.other.AccountEnums
import helpers.services.ServiceSpec

class ClassRoomServiceSpec extends ServiceSpec {

  val testUserId       = generateTestSystemId(USER)
  val testClassId      = generateTestSystemId("class")
  val testClassName    = "testClassRoomName"
  val testAccount      = testUserAccount(AccountEnums.teacher)
  val testBasicAccount = testUserAccount(AccountEnums.basic)

  val testService = new ClassRoomService {
    override val classRoomRepository   = mockClassRoomRepo
    override val userAccountRepository = mockUserAccountRepo
  }

  "createClassRoom" should {
    "return a MongoSuccessCreate response" in {
      mockGetUserBySelector(returned = testAccount)

      mockCreateNewClassRoom(created = true)

      awaitAndAssert(testService.createClassRoom(testClassName, testUserId)) {
        _ mustBe MongoSuccessCreate
      }
    }

    "throw an EnrolmentsNotFoundException" when {
      "the user has no deversityId" in {
        mockGetUserBySelector(returned = testBasicAccount)

        intercept[EnrolmentsNotFoundException](await(testService.createClassRoom(testClassName, testUserId)))
      }

      "the user has no enrolments" in {
        mockGetUserBySelector(returned = testAccount.copy(deversityDetails = None))

        intercept[EnrolmentsNotFoundException](await(testService.createClassRoom(testClassName, testUserId)))
      }
    }
  }

  "getClassesForTeachers" should {
    "return a populated list of class rooms" in {
      mockGetUserBySelector(returned = testAccount)

      mockGetClassesForTeacher(fetched = true)

      awaitAndAssert(testService.getClassesForTeachers(testUserId)) {
        _ mustBe testClassList
      }
    }

    "return an empty list of class rooms" in {
      mockGetUserBySelector(returned = testAccount)

      mockGetClassesForTeacher(fetched = false)

      awaitAndAssert(testService.getClassesForTeachers(testUserId)) {
        _ mustBe List()
      }
    }

    "throw an EnrolmentsNotFoundException" when {
      "the user has no deversityId" in {
        mockGetUserBySelector(returned = testBasicAccount)

        intercept[EnrolmentsNotFoundException](await(testService.getClassesForTeachers(testUserId)))
      }
    }
  }

  "getClassroom" should {
    "return some classroom" in {
      mockGetUserBySelector(returned = testAccount)

      mockGetClassRoom(fetched = true)

      awaitAndAssert(testService.getClassroom(testUserId, testClassId)) {
        _ mustBe Some(testClassRoom)
      }
    }

    "return none if no classroom was found" in {
      mockGetUserBySelector(returned = testAccount)

      mockGetClassRoom(fetched = false)

      awaitAndAssert(testService.getClassroom(testUserId, testClassId)) {
        _ mustBe None
      }
    }

    "throw a EnrolmentsNotFoundException if the fetched user doesn't have any enrolments" in {
      mockGetUserBySelector(returned = testUserAccount(AccountEnums.basic))

      intercept[EnrolmentsNotFoundException](await(testService.getClassroom(testUserId, testClassId)))
    }
  }

  "deleteClassRoom" should {
    "return a MongoSuccessDelete response" in {
      mockGetUserBySelector(returned = testAccount)

      mockDeleteClassRoom(deleted = true)

      awaitAndAssert(testService.deleteClassRoom(testUserId, testClassId)) {
        _ mustBe MongoSuccessDelete
      }
    }

    "return a MongoFailedDelete response" in {
      mockGetUserBySelector(returned = testAccount)

      mockDeleteClassRoom(deleted = false)

      awaitAndAssert(testService.deleteClassRoom(testUserId, testClassId)) {
        _ mustBe MongoFailedDelete
      }
    }

    "throw an EnrolmentsNotFoundException if the current user has no enrolments" in {
      mockGetUserBySelector(returned = testUserAccount(AccountEnums.basic))

      intercept[EnrolmentsNotFoundException](await(testService.deleteClassRoom(testUserId, testClassId)))
    }
  }
}
