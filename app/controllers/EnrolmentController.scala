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
import com.cjwwdev.mongo.responses.{MongoFailedUpdate, MongoSuccessUpdate}
import com.cjwwdev.security.encryption.DataSecurity
import common._
import models.DeversityEnrolment
import play.api.mvc.{Action, AnyContent, Result}
import services.EnrolmentService

import scala.concurrent.ExecutionContext.Implicits.global

class EnrolmentControllerImpl @Inject()(val authConnector: AuthConnector,
                                        val enrolmentService: EnrolmentService) extends EnrolmentController

trait EnrolmentController extends BackendController with Authorisation {
  val enrolmentService: EnrolmentService

  def createDeversityId(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        enrolmentService.createDeversityId(user.id) map { devId =>
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
      authorised(userId) { user =>
        enrolmentService.getEnrolment(user.id) map {
          _.fold[Result](NoContent)(enr => Ok(DataSecurity.encryptType(enr)))
        } recover {
          case _: MissingAccountException => NotFound
          case _                          => InternalServerError
        }
      }
    }
  }

  def updateDeversityInformation(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        withJsonBody[DeversityEnrolment](DeversityEnrolment.reads) { details =>
          enrolmentService.updateDeversityEnrolment(user.id, details) map {
            case MongoSuccessUpdate => Ok
            case MongoFailedUpdate  => InternalServerError
          }
        }
      }
    }
  }

  def generateRegistrationCode(userId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised(userId) { user =>
      enrolmentService.generateRegistrationCode(user.id) map { generated =>
        if(generated) Ok else InternalServerError
      }
    }
  }

  def getRegistrationCode(userId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised(userId) { user =>
      enrolmentService.getRegistrationCode(user.id) map { regCode =>
        Ok(regCode.encryptType)
      } recover {
        case _ => InternalServerError
      }
    }
  }

  def lookupRegistrationCode(userId: String, regCode: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        enrolmentService.lookupRegistrationCode(regCode) map { id =>
          Ok(id.encrypt)
        } recover {
          case _: RegistrationCodeNotFoundException => NotFound
          case _: RegistrationCodeExpiredException  => BadRequest
        }
      }
    }
  }
}
