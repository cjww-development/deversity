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

import com.cjwwdev.auth.actions.BaseAuth
import com.cjwwdev.config.ConfigurationLoader
import play.api.mvc.{Action, AnyContent}
import services.ValidationService
import utils.BackendController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class ValidationController @Inject()(val config: ConfigurationLoader,
                                     validationService: ValidationService) extends BackendController with BaseAuth {

  def validateSchool(schoolName: String): Action[AnyContent] = Action.async { implicit request =>
    openActionVerification {
      withEncryptedUrl(schoolName) { decryptedSchoolName =>
        validationService.validateSchool(decryptedSchoolName) map { found =>
          if(found) Ok else NotFound
        }
      }
    }
  }

  def validateTeacher(userName: String, schoolName: String): Action[AnyContent] = Action.async { implicit request =>
    openActionVerification {
      withEncryptedUrl(userName) { decryptedUserName =>
        withEncryptedUrl(schoolName) { decryptedSchoolName =>
          validationService.validateTeacher(decryptedUserName, decryptedSchoolName) map { found =>
            if(found) Ok else NotFound
          }
        }
      }
    }
  }
}
