/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package helpers.services

import com.cjwwdev.mongo.responses.{MongoCreateResponse, MongoDeleteResponse, MongoFailedCreate, MongoSuccessCreate}
import helpers.other.Fixtures
import models.ClassRoom
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.ClassRoomService
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockClassRoomService extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockClassRoomService = mock[ClassRoomService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClassRoomService)
  }

  def mockCreateNewClassRoom(created: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockClassRoomService.createClassRoom(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(created) MongoSuccessCreate else MongoFailedCreate))
  }

  def mockGetClassesForTeacher(classList: Future[List[ClassRoom]]): OngoingStubbing[Future[List[ClassRoom]]] = {
    when(mockClassRoomService.getClassesForTeachers(ArgumentMatchers.any()))
      .thenReturn(classList)
  }

  def mockGetClassRoom(classRoom: Future[Option[ClassRoom]]): OngoingStubbing[Future[Option[ClassRoom]]] = {
    when(mockClassRoomService.getClassroom(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(classRoom)
  }

  def mockDeleteClassRoom(response: Future[MongoDeleteResponse]): OngoingStubbing[Future[MongoDeleteResponse]] = {
    when(mockClassRoomService.deleteClassRoom(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(response)
  }
}
