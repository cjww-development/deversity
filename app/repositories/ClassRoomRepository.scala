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
package repositories

import javax.inject.Inject

import com.cjwwdev.logging.Logging
import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import com.cjwwdev.mongo.responses._
import models.ClassRoom
import play.api.Configuration
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.play.json._
import selectors.ClassRoomSelectors._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DefaultClassRoomRepository @Inject()(val config: Configuration) extends ClassRoomRepository with ConnectionSettings

trait ClassRoomRepository extends DatabaseRepository with Logging {

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("classId" -> IndexType.Ascending),
      name   = Some("ClassId"),
      unique = true,
      sparse = false
    )
  )

  def createNewClassRoom(classRoom: ClassRoom): Future[MongoCreateResponse] = {
    for {
      col <- collection
      wr  <- col.insert[ClassRoom](classRoom)
    } yield if(wr.ok) {
      MongoSuccessCreate
    } else {
      logger.error(s"[createNewClassRoom] - there was a problem creating a class for teacher ${classRoom.teacherDevId}")
      MongoFailedCreate
    }
  }

  def getClassesForTeacher(teacherDevId: String): Future[List[ClassRoom]] = {
    for {
      col  <- collection
      list <- col.find(teacherDevIdSelector(teacherDevId)).cursor[ClassRoom]().collect[List]()
    } yield list
  }

  def getClassroom(classId: String, teacherId: String): Future[Option[ClassRoom]] = {
    for {
      col  <- collection
      room <- col.find(classRoomSelector(classId, teacherId)).one[ClassRoom]
    } yield room
  }

  def deleteClassRoom(classId: String, teacherId: String): Future[MongoDeleteResponse] = {
    for {
      col <- collection
      wr  <- col.remove(classRoomSelector(classId, teacherId))
    } yield if(wr.ok) {
      MongoSuccessDelete
    } else {
      logger.error(s"[deleteClassRoom] - there was a problem deleting a class room with class id $classId and teacherDevId $teacherId")
      MongoFailedDelete
    }
  }
}
