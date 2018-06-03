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

package controllers

import com.cjwwdev.mongo.responses.{MongoFailedDelete, MongoSuccessDelete}
import com.cjwwdev.implicits.ImplicitDataSecurity._
import common.{EnrolmentsNotFoundException, MissingAccountException}
import helpers.controllers.ControllerSpec
import models.ClassRoom
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ClassRoomControllerSpec extends ControllerSpec {

  val testController = new ClassRoomController {
    override protected def controllerComponents = stubControllerComponents()
    override val classRoomService               = mockClassRoomService
    override val authConnector                  = mockAuthConnector
  }

  "createNewClassRoom" should {
    val request: FakeRequest[String] = standardRequest.withBody("testClassRoom".encrypt)

    "return an Ok" when {
      "a new classroom has been made" in {
        mockCreateNewClassRoom(created = true)

        runActionWithAuth(testController.createNewClassRoom(testUserId), request, "individual") {
          status(_) mustBe CREATED
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem creating the classroom" in {
        mockCreateNewClassRoom(created = false)

        runActionWithAuth(testController.createNewClassRoom(testUserId), request, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getClassesForTeacher" should {
    "return an Ok" when {
      "the classrooms have found for the user" in {
        mockGetClassesForTeacher(classList = Future(testClassList))

        runActionWithAuth(testController.getClassesForTeacher(testUserId), standardRequest, "individual") { res =>
          status(res)                                           mustBe OK
          contentAsJson(res).\("body").as[String].decryptIntoType[List[ClassRoom]] mustBe testClassList
        }
      }
    }

    "return a NotFound" when {
      "no classrooms have been found for the user" in {
        mockGetClassesForTeacher(classList = Future.failed(new MissingAccountException("")))

        runActionWithAuth(testController.getClassesForTeacher(testUserId), standardRequest, "individual") {
          status(_) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem getting the classrooms for the user" in {
        mockGetClassesForTeacher(classList = Future.failed(new Exception))

        runActionWithAuth(testController.getClassesForTeacher(testUserId), standardRequest, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getClassRoom" should {
    "return an Ok" when {
      "the given class name and user id matches a classroom" in {
        mockGetClassRoom(classRoom = Future(Some(testClassRoom)))

        runActionWithAuth(testController.getClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") { res =>
          status(res)                                     mustBe OK
          contentAsJson(res).\("body").as[String].decryptIntoType[ClassRoom] mustBe testClassRoom
        }
      }
    }

    "return a NotFound" when {
      "no classroom can be found" in {
        mockGetClassRoom(classRoom = Future(None))

        runActionWithAuth(testController.getClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") { res =>
          status(res) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem finding a classroom for a user" in {
        mockGetClassRoom(classRoom = Future.failed(new Exception))

        runActionWithAuth(testController.getClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") { res =>
          status(res) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "deleteClassRoom" should {
    "return an Ok" when {
      "a class room has been deleted" in {
        mockDeleteClassRoom(response = Future(MongoSuccessDelete))

        runActionWithAuth(testController.deleteClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") {
          status(_) mustBe OK
        }
      }
    }

    "return a NotFound" when {
      "no account could be found" in {
        mockDeleteClassRoom(response = Future.failed(new MissingAccountException("")))

        runActionWithAuth(testController.deleteClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") {
          status(_) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem deleting" in {
        mockDeleteClassRoom(response = Future(MongoFailedDelete))

        runActionWithAuth(testController.deleteClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }

      "a EnrolmentsNotFoundException was caught" in {
        mockDeleteClassRoom(response = Future.failed(new EnrolmentsNotFoundException("")))

        runActionWithAuth(testController.deleteClassRoom(testUserId, generateTestSystemId("class")), standardRequest, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }
}
