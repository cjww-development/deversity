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
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._
import com.cjwwdev.security.deobfuscation.DeObfuscation._
import common.BackendController
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ValidationService

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultValidationController @Inject()(val validationService: ValidationService,
                                            val config: ConfigurationLoader,
                                            val controllerComponents: ControllerComponents) extends ValidationController {
  override val appId: String = config.getServiceId(config.get[String]("appName"))
}

trait ValidationController extends BackendController with BaseAuth {
  val validationService: ValidationService

  def validateSchool(regCode: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      withEncryptedUrl[String](regCode) { decryptedRegCode =>
        validationService.validateSchool(decryptedRegCode) map { schoolDevId =>
          withJsonResponseBody(OK, schoolDevId.encrypt) { json =>
            Ok(json)
          }
        } recover {
          case _ => withJsonResponseBody(NOT_FOUND, "Registration code could not be validated") { json =>
            NotFound(json)
          }
        }
      }
    }
  }

  def validateTeacher(regCode: String, schoolDevId: String): Action[AnyContent] = Action.async { implicit request =>
    applicationVerification {
      withEncryptedUrl[String](regCode) { decryptedRegCode =>
        withEncryptedUrl[String](schoolDevId) { decryptedSchoolDevId =>
          validationService.validateTeacher(decryptedRegCode, decryptedSchoolDevId) map { teacherDevId =>
            withJsonResponseBody(OK, teacherDevId.encrypt) { json =>
              Ok(json)
            }
          } recover {
            case _ => withJsonResponseBody(NOT_FOUND, "Registration code could not be validated") { json =>
              NotFound(json)
            }
          }
        }
      }
    }
  }
}
