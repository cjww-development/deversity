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
import com.cjwwdev.request.RequestParsers
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.security.obfuscation.Obfuscation._
import com.cjwwdev.mongo.responses.{MongoFailedUpdate, MongoSuccessUpdate}
import common._
import javax.inject.Inject
import models.DeversityEnrolment
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.EnrolmentService

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultEnrolmentController @Inject()(val authConnector: AuthConnector,
                                           val controllerComponents: ControllerComponents,
                                           val config: ConfigurationLoader,
                                           val enrolmentService: EnrolmentService) extends EnrolmentController {
  override val appId: String = config.getServiceId(config.get[String]("appName"))
}

trait EnrolmentController extends BackendController with Authorisation {
  val enrolmentService: EnrolmentService

  private val parsers = new RequestParsers {}

  def createDeversityId(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        enrolmentService.createDeversityId(user.id) map { devId =>
          withJsonResponseBody(OK, devId) { json =>
            Ok(json)
          }
        } recover {
          case _: AlreadyExistsException => withJsonResponseBody(CONFLICT, "Current user already has a deversity enrolment") { json =>
            Conflict(json)
          }
          case _: MissingAccountException => withJsonResponseBody(NOT_FOUND, "No account found") { json =>
            NotFound(json)
          }
          case _ => withJsonResponseBody(INTERNAL_SERVER_ERROR, "There was a problem creating the users deversity enrolment") { json =>
            InternalServerError(json)
          }
        }
      }
    }
  }

  def getDeversityEnrolment(userId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        enrolmentService.getEnrolment(user.id) map { enr =>
          val (status, body) = enr.fold[(Int, JsValue)]((NO_CONTENT, ""))(enr => (OK, enr.encrypt))
          withJsonResponseBody(status, body) { json =>
            status match {
              case OK         => Ok(json)
              case NO_CONTENT => NoContent
            }
          }
        } recover {
          case _: MissingAccountException => withJsonResponseBody(NO_CONTENT, "No account found") { json =>
            NotFound(json)
          }
          case _ => withJsonResponseBody(INTERNAL_SERVER_ERROR, "There was problem getting the deversity enrolment") { json =>
            InternalServerError(json)
          }
        }
      }
    }
  }

  def updateDeversityEnrolment(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        parsers.withJsonBody[DeversityEnrolment] { details =>
          enrolmentService.updateDeversityEnrolment(user.id, details) map { resp =>
            val (status, body) = resp match {
              case MongoSuccessUpdate => (OK, "Deversity enrolment has been updated")
              case MongoFailedUpdate  => (INTERNAL_SERVER_ERROR, "There was a problem updating the deversity enrolment")
            }

            withJsonResponseBody(status, body) { json =>
              status match {
                case OK                    => Ok(json)
                case INTERNAL_SERVER_ERROR => InternalServerError(json)
              }
            }
          }
        }
      }
    }
  }

  def generateRegistrationCode(userId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised(userId) { user =>
      enrolmentService.generateRegistrationCode(user.id) map { generated =>
        val (status, body) = if(generated) {
          (OK, "Registration code generated")
        } else {
          (INTERNAL_SERVER_ERROR, "There was a problem generating the registration code")
        }

        withJsonResponseBody(status, body) { json =>
          status match {
            case OK                    => Ok(json)
            case INTERNAL_SERVER_ERROR => InternalServerError(json)
          }
        }
      }
    }
  }

  def getRegistrationCode(userId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised(userId) { user =>
      enrolmentService.getRegistrationCode(user.id) map { regCode =>
        withJsonResponseBody(OK, regCode.encrypt) { json =>
          Ok(json)
        }
      } recover {
        case _ => withJsonResponseBody(INTERNAL_SERVER_ERROR, "There was a problem getting the reg code") { json =>
          InternalServerError(json)
        }
      }
    }
  }

  def lookupRegistrationCode(userId: String, regCode: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { _ =>
        enrolmentService.lookupRegistrationCode(regCode) map { id =>
          withJsonResponseBody(OK, id.encrypt) { json =>
            Ok(json)
          }
        } recover {
          case _: RegistrationCodeNotFoundException => withJsonResponseBody(NOT_FOUND, "Could not match registration code to a user") { json =>
            NotFound(json)
          }
          case _: RegistrationCodeExpiredException  => withJsonResponseBody(BAD_REQUEST, "Registration code for user has expired") { json =>
            BadRequest(json)
          }
        }
      }
    }
  }
}
