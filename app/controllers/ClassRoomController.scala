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
import com.cjwwdev.mongo.responses.{MongoFailedCreate, MongoFailedDelete, MongoSuccessCreate, MongoSuccessDelete}
import com.cjwwdev.security.encryption.DataSecurity
import common.{BackendController, EnrolmentsNotFoundException, MissingAccountException}
import models.ClassRoom._
import play.api.mvc.{Action, AnyContent, Result}
import services.ClassRoomService

import scala.concurrent.ExecutionContext.Implicits.global

class ClassRoomControllerImpl @Inject()(val classRoomService: ClassRoomService,
                                        val authConnector: AuthConnector) extends ClassRoomController

trait ClassRoomController extends BackendController with Authorisation {
  val classRoomService: ClassRoomService

  def createNewClassRoom(userId: String): Action[String] = Action.async(parse.text) { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.createClassRoom(request.body.decrypt, user.id) map {
          case MongoSuccessCreate => Created
          case MongoFailedCreate  => InternalServerError
        }
      }
    }
  }

  def getClassesForTeacher(userId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.getClassesForTeachers(user.id) map { list =>
          Ok(DataSecurity.encryptType(list))
        } recover {
          case _: MissingAccountException => NotFound
          case _                          => InternalServerError
        }
      }
    }
  }

  def getClassRoom(userId: String, classId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.getClassroom(user.id, classId) map {
          _.fold[Result](NotFound)(classRoom => Ok(classRoom.encryptType))
        } recover {
          case _ => InternalServerError
        }
      }
    }
  }

  def deleteClassRoom(userId: String, classId: String): Action[AnyContent] = Action.async { implicit request =>
    validateAs(USER, userId) {
      authorised(userId) { user =>
        classRoomService.deleteClassRoom(user.id, classId) map {
          case MongoSuccessDelete => Ok
          case MongoFailedDelete  => InternalServerError
        } recover {
          case _: EnrolmentsNotFoundException => InternalServerError
          case _: MissingAccountException     => NotFound
        }
      }
    }
  }
}
