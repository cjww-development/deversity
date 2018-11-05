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
import common.MissingAccountException
import helpers.controllers.ControllerSpec
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ValidationControllerSpec extends ControllerSpec {

  val testController = new ValidationController {
    override protected def controllerComponents = stubControllerComponents()
    override val validationService              = mockValidationService
    override val appId                          = "testAppId"
  }

  val encryptedSchoolName = "tSchoolName".encrypt
  val encryptedUserName   = createTestUserName.encrypt

  "validateSchool" should {
    "return an ok" when {
      "the school has been successfully validated" in {
        mockValidateSchool(returned = Future("testOrgDevId"))

        assertResult(testController.validateSchool(encryptedSchoolName)(standardRequest)) {
          status(_) mustBe OK
        }
      }
    }

    "return a Not found" when {
      "the school has not been validated" in {
        mockValidateSchool(returned = Future.failed(new MissingAccountException("")))

        assertResult(testController.validateSchool(encryptedSchoolName)(standardRequest)) {
          status(_) mustBe NOT_FOUND
        }
      }
    }
  }

  "validateTeacher" should {
    "return an ok" when {
      "the teacher has been successfully validated" in {
        mockValidateTeacher(returned = Future(testDeversityId))

        assertResult(testController.validateTeacher(encryptedUserName, encryptedSchoolName)(standardRequest)) {
          status(_) mustBe OK
        }
      }
    }

    "return a Not found" when {
      "the teacher has not been validated" in {
        mockValidateTeacher(returned = Future.failed(new MissingAccountException("")))

        assertResult(testController.validateTeacher(encryptedUserName, encryptedSchoolName)(standardRequest)) {
          status(_) mustBe NOT_FOUND
        }
      }
    }
  }
}
