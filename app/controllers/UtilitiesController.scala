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

import com.cjwwdev.auth.backend.Authorisation
import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.deobfuscation.DeObfuscation._
import common.BackendController
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.UtilitiesService

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultUtilitiesController @Inject()(val utilitiesService: UtilitiesService,
                                           val controllerComponents: ControllerComponents,
                                           val config: ConfigurationLoader,
                                           val authConnector: AuthConnector) extends UtilitiesController {
  override val appId: String = config.getServiceId(config.get[String]("appName"))
}

trait UtilitiesController extends BackendController with Authorisation {
  val utilitiesService: UtilitiesService

  def getSchoolDetails(userId: String, schoolDevId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        withEncryptedUrl[String](schoolDevId) { oDId =>
          utilitiesService.getSchoolDetails(oDId) map { details =>
            withJsonResponseBody(OK, details.encrypt) { json =>
              Ok(json)
            }
          } recover {
            case _ => withJsonResponseBody(NOT_FOUND, "No school details found") { json =>
              NotFound(json)
            }
          }
        }
      }
    }
  }

  def getTeacherDetails(userId: String, teacherDevId: String, schoolDevId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        withEncryptedUrl[String](teacherDevId) { tDevId =>
          withEncryptedUrl[String](schoolDevId) { sDevId =>
            utilitiesService.getTeacherDetails(tDevId, sDevId) map { teacherDetails =>
              withJsonResponseBody(OK, teacherDetails.encrypt) { json =>
                Ok(json)
              }
            } recover {
              case _ => withJsonResponseBody(NOT_FOUND, "No teacher details found") { json =>
                NotFound(json)
              }
            }
          }
        }
      }
    }
  }
}
