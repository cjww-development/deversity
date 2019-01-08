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
package services

import java.util.UUID

import com.cjwwdev.implicits.ImplicitJsValues._
import com.cjwwdev.mongo.responses.{MongoCreateResponse, MongoDeleteResponse}
import common.EnrolmentsNotFoundException
import javax.inject.Inject
import models.ClassRoom
import repositories.{ClassRoomRepository, UserAccountRepository}
import selectors.UserAccountSelectors.userIdSelector

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultClassRoomService @Inject()(val classRoomRepository: ClassRoomRepository,
                                        val userAccountRepository: UserAccountRepository) extends ClassRoomService

trait ClassRoomService {
  val classRoomRepository: ClassRoomRepository
  val userAccountRepository: UserAccountRepository

  private def generateClassRoom(className: String, teacher: String, school: String)(implicit ec: ExC): ClassRoom = ClassRoom(
    classId      = s"class-${UUID.randomUUID()}",
    schooldevId  = school,
    teacherDevId = teacher,
    name         = className
  )

  def createClassRoom(className: String, userId: String)(implicit ec: ExC): Future[MongoCreateResponse] = {
    for {
      acc        <- userAccountRepository.getUserBySelector(userIdSelector(userId))
      enrs       =  acc.enrolments.getOrElse(throw new EnrolmentsNotFoundException(s"No enrolments for user ${acc.userId}"))
      devDetails =  acc.deversityDetails.getOrElse(throw new EnrolmentsNotFoundException(s"No deversity details for user ${acc.userId}"))
      classRoom  =  generateClassRoom(className, enrs.get[String]("deversityId"), devDetails.schoolDevId)
      resp       <- classRoomRepository.createNewClassRoom(classRoom)
    } yield resp
  }

  def getClassesForTeachers(userId: String)(implicit ec: ExC): Future[List[ClassRoom]] = {
    for {
      acc  <- userAccountRepository.getUserBySelector(userIdSelector(userId))
      enrs =  acc.enrolments.getOrElse(throw new EnrolmentsNotFoundException(s"No deversity details for user ${acc.userId}"))
      list <- classRoomRepository.getClassesForTeacher(enrs.get[String]("deversityId"))
    } yield list
  }

  def getClassroom(userId: String, classId: String)(implicit ec: ExC): Future[Option[ClassRoom]] = {
    for {
      acc  <- userAccountRepository.getUserBySelector(userIdSelector(userId))
      enrs =  acc.enrolments.getOrElse(throw new EnrolmentsNotFoundException(s"No deversity details for user ${acc.userId}"))
      room <- classRoomRepository.getClassroom(classId, enrs.get[String]("deversityId"))
    } yield room
  }

  def deleteClassRoom(userId: String, classId: String)(implicit ec: ExC): Future[MongoDeleteResponse] = {
    for {
      acc  <- userAccountRepository.getUserBySelector(userIdSelector(userId))
      enrs =  acc.enrolments.getOrElse(throw new EnrolmentsNotFoundException(s"No deversity details for user ${acc.userId}"))
      resp <- classRoomRepository.deleteClassRoom(classId, enrs.get[String]("deversityId"))
    } yield resp
  }
}
