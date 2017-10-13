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
import com.cjwwdev.reactivemongo.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import common.{AlreadyExistsException, MissingAccountException}
import models.DeversityEnrolment
import play.api.mvc.{Action, AnyContent}
import services.EnrolmentService
import utils.BackendController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EnrolmentController @Inject()(val authConnector: AuthConnector,
                                    val config: ConfigurationLoader,
                                    enrolmentService: EnrolmentService) extends BackendController with Authorisation {
  def createDeversityId(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { context =>
        enrolmentService.createDeversityId(context.user.id) map { devId =>
          Ok(devId)
        } recover {
          case _: AlreadyExistsException  => Conflict
          case _: MissingAccountException => NotFound
          case _                          => InternalServerError
        }
      }
    }
  }

  def getDeversityEnrolment(userId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { context =>
        enrolmentService.getEnrolment(context.user.id) map {
          case Some(enr) => Ok(DataSecurity.encryptType[DeversityEnrolment](enr))
          case None      => NotFound
        } recover {
          case _: MissingAccountException => NotFound
          case _                          => InternalServerError
        }
      }
    }
  }

  def getDeversityEnrolmentForConfirmation(orgId: String, userId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(ORG_USER, orgId) {
      authorised(orgId) { _ =>
        enrolmentService.getEnrolment(userId) map { enr =>
          Ok(DataSecurity.encryptType(enr))
        } recover {
          case _: MissingAccountException => NotFound
          case _                          => InternalServerError
        }
      }
    }
  }

  def updateDeversityInformation(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { context =>
        withJsonBody[DeversityEnrolment](DeversityEnrolment.reads) { details =>
          enrolmentService.updateDeversityEnrolment(userId, details) map {
            case MongoSuccessUpdate => Ok
            case MongoFailedUpdate  => InternalServerError
          }
        }
      }
    }
  }
}
