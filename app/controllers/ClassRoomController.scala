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
import com.cjwwdev.mongo.responses.{MongoFailedCreate, MongoFailedDelete, MongoSuccessCreate, MongoSuccessDelete}
import common.{BackendController, EnrolmentsNotFoundException, MissingAccountException}
import javax.inject.Inject
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.ClassRoomService

import scala.concurrent.ExecutionContext.Implicits.global

class DefaultClassRoomController @Inject()(val classRoomService: ClassRoomService,
                                           val controllerComponents: ControllerComponents,
                                           val config: ConfigurationLoader,
                                           val authConnector: AuthConnector) extends ClassRoomController {
  override val appId: String = config.getServiceId(config.get[String]("appName"))
}

trait ClassRoomController extends BackendController with Authorisation {
  val classRoomService: ClassRoomService

  def createNewClassRoom(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        withEncryptedUrl[String](request.body) { classRoom =>
          classRoomService.createClassRoom(classRoom, user.id) map {
            case MongoSuccessCreate => withJsonResponseBody(CREATED, s"Created class room $classRoom") { json =>
              Created(json)
            }
            case MongoFailedCreate  => withJsonResponseBody(INTERNAL_SERVER_ERROR, s"There was problem creating classroom $classRoom") { json =>
              InternalServerError(json)
            }
          }
        }
      }
    }
  }

  def getClassesForTeacher(userId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.getClassesForTeachers(user.id) map { list =>
          val (status, body) = if(list.nonEmpty) (OK, list.encrypt) else (NO_CONTENT, "No classes found the given user")
          withJsonResponseBody(status, body) { json =>
            status match {
              case OK         => Ok(json)
              case NO_CONTENT => NoContent
            }
          }
        } recover {
          case _: MissingAccountException => withJsonResponseBody(NOT_FOUND, "No account found") { json =>
            NotFound(json)
          }
          case _ => withJsonResponseBody(INTERNAL_SERVER_ERROR, "There was a problem retrieving classes for the teacher") { json =>
            InternalServerError(json)
          }
        }
      }
    }
  }

  def getClassRoom(userId: String, classId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.getClassroom(user.id, classId) map { classRoom =>
          val (status, body) = classRoom.fold((NOT_FOUND, s"No class room found for $classId"))(room => (OK, room.encrypt))
          withJsonResponseBody(status, body) { json =>
            status match {
              case OK        => Ok(json)
              case NOT_FOUND => NotFound(json)
            }
          }
        } recover {
          case _ => withJsonResponseBody(INTERNAL_SERVER_ERROR, s"There was a problem getting the requested classroom") { json =>
            InternalServerError(json)
          }
        }
      }
    }
  }

  def deleteClassRoom(userId: String, classId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.deleteClassRoom(user.id, classId) map { resp =>
          val (status, body) = resp match {
            case MongoSuccessDelete => (OK, "Classroom deleted")
            case MongoFailedDelete  => (INTERNAL_SERVER_ERROR, "There was a problem deleting the classroom")
          }

          withJsonResponseBody(status, body) { json =>
            status match {
              case OK                    => Ok(json)
              case INTERNAL_SERVER_ERROR => InternalServerError(json)
            }
          }
        } recover {
          case _: EnrolmentsNotFoundException => withJsonResponseBody(INTERNAL_SERVER_ERROR, "Appropriate enrolments couldn't be found") { json =>
            InternalServerError(json)
          }
          case _: MissingAccountException => withJsonResponseBody(NOT_FOUND, "No account found") { json =>
            NotFound(json)
          }
        }
      }
    }
  }
}
