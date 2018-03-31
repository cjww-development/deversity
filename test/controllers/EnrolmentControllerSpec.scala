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
package controllers

import com.cjwwdev.security.encryption.DataSecurity
import common.{AlreadyExistsException, MissingAccountException, RegistrationCodeExpiredException, RegistrationCodeNotFoundException}
import helpers.controllers.ControllerSpec
import models.{DeversityEnrolment, RegistrationCode}
import models.formatters.MongoFormatting
import play.api.libs.json.{Format, JsSuccess, OWrites}
import play.api.test.FakeRequest

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentControllerSpec extends ControllerSpec {

  val testController = new EnrolmentController {
    override val enrolmentService = mockEnrolmentService
    override val authConnector    = mockAuthConnector
  }

  implicit val enrolmentFormatter: Format[DeversityEnrolment] = DeversityEnrolment.format(MongoFormatting)
  implicit val enrolmentWrites: OWrites[DeversityEnrolment] = DeversityEnrolment.writes

  "createDeversityId" should {
    "return an Ok" when {
      "a deversity id has been created for the user" in {
        val request = standardRequest.withBody("")

        mockCreateDeversityId(returned = Future.successful(DataSecurity.encryptString(testDeversityId)))

        runActionWithAuth(testController.createDeversityId(testUserId), request, "individual") {
          status(_) mustBe OK
        }
      }
    }

    "return a Conflict" when {
      "when a user exists but the user already has an deversity id" in {
        val request = standardRequest.withBody("")

        mockCreateDeversityId(returned = Future.failed(new AlreadyExistsException("")))

        runActionWithAuth(testController.createDeversityId(testUserId), request, "individual") {
          status(_) mustBe CONFLICT
        }
      }
    }

    "return a NotFound" when {
      "the user doesn't exist" in {
        val request = standardRequest.withBody("")

        mockCreateDeversityId(returned = Future.failed(new MissingAccountException("")))

        runActionWithAuth(testController.createDeversityId(testUserId), request, "individual") {
          status(_) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was some other problem" in {
        val request = standardRequest.withBody("")

        mockCreateDeversityId(returned = Future.failed(new IllegalStateException("")))

        runActionWithAuth(testController.createDeversityId(testUserId), request, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getDeversityEnrolment" should {
    "return an Ok" when {
      "the users enrolment has been fetched" in {
        mockGetDeversityEnrolment(returned = Future(Some(testStudentEnrolment)))

        runActionWithAuth(testController.getDeversityEnrolment(testUserId), standardRequest, "individual") { result =>
          status(result) mustBe OK
          DataSecurity.decryptIntoType[DeversityEnrolment](contentAsString(result)) mustBe JsSuccess(testStudentEnrolment)
        }
      }
    }

    "return a NotFound" when {
      "a user can't be found against the given id" in {
        mockGetDeversityEnrolment(returned = Future.failed(new MissingAccountException("")))

        runActionWithAuth(testController.getDeversityEnrolment(testUserId), standardRequest, "individual") {
          status(_) mustBe NOT_FOUND
        }
      }
    }

    "return an InternalServerError" when {
      "there was some other problem" in {
        mockGetDeversityEnrolment(returned = Future.failed(new IllegalStateException("")))

        runActionWithAuth(testController.getDeversityEnrolment(testUserId), standardRequest, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "updateDeversityInformation" should {
    "return an Ok" when {
      "the users deversity information has been updated" in {
        val request: FakeRequest[String] = standardRequest.withBody(testStudentEnrolment.encryptType)

        mockUpdateDeversityEnrolment(success = true)

        runActionWithAuth(testController.updateDeversityInformation(testUserId), request, "individual") {
          status(_) mustBe OK
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem updating the users deversity information" in {
        val request: FakeRequest[String] = standardRequest.withBody(testStudentEnrolment.encryptType)

        mockUpdateDeversityEnrolment(success = false)

        runActionWithAuth(testController.updateDeversityInformation(testUserId), request, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "generateRegistrationCode" should {
    "return an Ok" when {
      "a registration code has been generated" in {
        mockGenerateRegistrationCode(generated = true)

        runActionWithAuth(testController.generateRegistrationCode(testUserId), standardRequest, "individual") {
          status(_) mustBe OK
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem generating a registration code" in {
        mockGenerateRegistrationCode(generated = false)

        runActionWithAuth(testController.generateRegistrationCode(testUserId), standardRequest, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "getRegistrationCode" should {
    "return an OK" when {
      "the registration code for the user has been found" in {
        mockGetRegistrationCode(fetched = true)

        runActionWithAuth(testController.getRegistrationCode(testUserId), standardRequest, "individual") { res =>
          status(res)                                        mustBe OK
          contentAsString(res).decryptType[RegistrationCode] mustBe testRegistrationCode
        }
      }
    }

    "return an InternalServerError" when {
      "the registration code was not found" in {
        mockGetRegistrationCode(fetched = false)

        runActionWithAuth(testController.getRegistrationCode(testUserId), standardRequest, "individual") {
          status(_) mustBe INTERNAL_SERVER_ERROR
        }
      }
    }
  }

  "lookupRegistrationCode" should {
    "return an Ok" when {
      "a registration code has been looked up" in {
        mockLookupRegistrationCode(regCode = Future(testUserId))

        runActionWithAuth(testController.lookupRegistrationCode(testUserId, "testRegCode"), standardRequest, "individual") { res =>
          status(res)                  mustBe OK
          contentAsString(res).decrypt mustBe testUserId
        }
      }
    }

    "return a NotFound" when {
      "a registration code has not been found" in {
        mockLookupRegistrationCode(regCode = Future.failed(new RegistrationCodeNotFoundException("")))

        runActionWithAuth(testController.lookupRegistrationCode(testUserId, "testRegCode"), standardRequest, "individual") {
          status(_) mustBe NOT_FOUND
        }
      }
    }

    "return a BadRequest" when {
      "the registration code has expired" in {
        mockLookupRegistrationCode(regCode = Future.failed(new RegistrationCodeExpiredException("")))

        runActionWithAuth(testController.lookupRegistrationCode(testUserId, "testRegCode"), standardRequest, "individual") {
          status(_) mustBe BAD_REQUEST
        }
      }
    }
  }
}
