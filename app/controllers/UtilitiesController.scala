// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package controllers

import javax.inject.{Inject, Singleton}

import com.cjwwdev.auth.actions.Authorisation
import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.security.encryption.DataSecurity
import common.AlreadyExistsException
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import services.UtilitiesService
import utils.BackendController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class UtilitiesController @Inject()(utilitiesService: UtilitiesService,
                                    val config: ConfigurationLoader,
                                    val authConnector: AuthConnector) extends BackendController with Authorisation {

  def getPendingEnrolmentsCount(orgId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(ORG_USER, orgId) {
      authorised(orgId) { context =>
        utilitiesService.getPendingEnrolmentCount(context.user.id) map { count =>
          Ok(DataSecurity.encryptType(count))
        } recover {
          case e =>
            Logger.error(s"[UtilitiesController] - [getPendingEnrolmentCount] - There was a problem getting the count for org id $orgId", e)
            InternalServerError
        }
      }
    }
  }

  def getSchoolDetails(userId: String, orgUserName: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        withEncryptedUrl(orgUserName) { oUN =>
          utilitiesService.getSchoolDetails(oUN) map { details =>
            Ok(DataSecurity.encryptType(details))
          } recover {
            case _ => NotFound
          }
        }
      }
    }
  }

  def getTeacherDetails(userId: String, tUserName: String, schoolName: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { context =>
        withEncryptedUrl(tUserName) { tDecName =>
          withEncryptedUrl(schoolName) { schoolDecName =>
            utilitiesService.getTeacherDetails(tDecName, schoolDecName) map { teacherDetails =>
              Ok(DataSecurity.encryptType(teacherDetails))
            } recover {
              case _ => NotFound
            }
          }
        }
      }
    }
  }
}
