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

import javax.inject.Inject

import com.cjwwdev.auth.backend.BaseAuth
import common.BackendController
import play.api.mvc.{Action, AnyContent}
import services.ValidationService

import scala.concurrent.ExecutionContext.Implicits.global

class ValidationControllerImpl @Inject()(val validationService: ValidationService) extends ValidationController

trait ValidationController extends BackendController with BaseAuth {
  val validationService: ValidationService

  def validateSchool(regCode: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      withEncryptedUrl(regCode) { decryptedRegCode =>
        validationService.validateSchool(decryptedRegCode) map { schoolDevId =>
          Ok(schoolDevId.encrypt)
        } recover {
          case _ => NotFound
        }
      }
    }
  }

  def validateTeacher(regCode: String, schoolDevId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      withEncryptedUrl(regCode) { decryptedRegCode =>
        withEncryptedUrl(schoolDevId) { decryptedSchoolDevId =>
          validationService.validateTeacher(decryptedRegCode, decryptedSchoolDevId) map { teacherDevId =>
            Ok(teacherDevId.encrypt)
          } recover {
            case _ => NotFound
          }
        }
      }
    }
  }
}
