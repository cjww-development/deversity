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

import com.cjwwdev.auth.backend.Authorisation
import com.cjwwdev.auth.connectors.AuthConnector
import common.BackendController
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import services.UtilitiesService

import scala.concurrent.ExecutionContext.Implicits.global

class UtilitiesControllerImpl @Inject()(val utilitiesService: UtilitiesService,
                                        val authConnector: AuthConnector) extends UtilitiesController

trait UtilitiesController extends BackendController with Authorisation {
  val utilitiesService: UtilitiesService

  def getPendingEnrolmentsCount(orgId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(ORG_USER, orgId) {
      authorised(orgId) { user =>
        utilitiesService.getPendingEnrolmentCount(user.id) map { count =>
          Ok(count.toString.encrypt)
        } recover {
          case e =>
            logger.error(s"[UtilitiesController] - [getPendingEnrolmentCount] - There was a problem getting the count for org id $orgId", e)
            InternalServerError
        }
      }
    }
  }

  def getSchoolDetails(userId: String, orgDevId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        withEncryptedUrl(orgDevId) { oDId =>
          utilitiesService.getSchoolDetails(oDId) map { details =>
            Ok(details.encryptType)
          } recover {
            case _ => NotFound
          }
        }
      }
    }
  }

  def getTeacherDetails(userId: String, teacherDevId: String, schoolDevId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        withEncryptedUrl(teacherDevId) { tDevId =>
          withEncryptedUrl(schoolDevId) { sDevId =>
            utilitiesService.getTeacherDetails(tDevId, sDevId) map { teacherDetails =>
              Ok(teacherDetails.encryptType)
            } recover {
              case _ => NotFound
            }
          }
        }
      }
    }
  }
}
