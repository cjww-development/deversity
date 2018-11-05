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

import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._
import com.cjwwdev.security.deobfuscation.DeObfuscation._
import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import common.{AlreadyExistsException, MissingAccountException, RegistrationCodeExpiredException, RegistrationCodeNotFoundException}
import helpers.controllers.ControllerSpec
import models.formatters.MongoFormatting
import models.{DeversityEnrolment, RegistrationCode}
import play.api.libs.json.{Format, JsSuccess, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EnrolmentControllerSpec extends ControllerSpec {

  val testController = new EnrolmentController {
    override protected def controllerComponents = stubControllerComponents()
    override val enrolmentService               = mockEnrolmentService
    override val authConnector                  = mockAuthConnector
    override val appId                          = "testAppId"
  }

  implicit val enrolmentFormatter: Format[DeversityEnrolment] = DeversityEnrolment.format(MongoFormatting)

  "createDeversityId" should {
    "return an Ok" when {
      "a deversity id has been created for the user" in {
        val request = standardRequest.withBody("")

        mockCreateDeversityId(returned = Future.successful(testDeversityId.encrypt))

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
        implicit val deObfuscator: DeObfuscator[DeversityEnrolment] = new DeObfuscator[DeversityEnrolment] {
          override def decrypt(value: String): Either[DeversityEnrolment, DecryptionError] = DeObfuscation.deObfuscate(value)
        }

        mockGetDeversityEnrolment(returned = Future(Some(testStudentEnrolment)))

        runActionWithAuth(testController.getDeversityEnrolment(testUserId), standardRequest, "individual") { result =>
          status(result) mustBe OK
          contentAsJson(result).\("body").as[String].decrypt[DeversityEnrolment] mustBe Left(testStudentEnrolment)
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

  "updateDeversityEnrolment" should {
    "return an Ok" when {
      "the users deversity information has been updated" in {
        val request: FakeRequest[String] = standardRequest.withBody(testStudentEnrolment.encrypt)

        mockUpdateDeversityEnrolment(success = true)

        runActionWithAuth(testController.updateDeversityEnrolment(testUserId), request, "individual") {
          status(_) mustBe OK
        }
      }
    }

    "return an InternalServerError" when {
      "there was a problem updating the users deversity information" in {
        val request: FakeRequest[String] = standardRequest.withBody(testStudentEnrolment.encrypt)

        mockUpdateDeversityEnrolment(success = false)

        runActionWithAuth(testController.updateDeversityEnrolment(testUserId), request, "individual") {
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
        implicit val deObfuscator: DeObfuscator[RegistrationCode] = new DeObfuscator[RegistrationCode] {
          override def decrypt(value: String): Either[RegistrationCode, DecryptionError] = {
            DeObfuscation.deObfuscate[RegistrationCode](value)
          }
        }

        mockGetRegistrationCode(fetched = true)

        runActionWithAuth(testController.getRegistrationCode(testUserId), standardRequest, "individual") { res =>
          status(res)                                                       mustBe OK
          contentAsJson(res).\("body").as[String].decrypt[RegistrationCode] mustBe Left(testRegistrationCode)
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
          status(res)                                             mustBe OK
          contentAsJson(res).\("body").as[String].decrypt[String] mustBe Left(testUserId)
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
