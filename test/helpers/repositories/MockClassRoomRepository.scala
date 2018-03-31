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

package helpers.repositories

import com.cjwwdev.mongo.responses._
import helpers.other.Fixtures
import models.ClassRoom
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import repositories.ClassRoomRepository
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockClassRoomRepository extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockClassRoomRepo = mock[ClassRoomRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClassRoomRepo)
  }

  def mockCreateNewClassRoom(created: Boolean): OngoingStubbing[Future[MongoCreateResponse]] = {
    when(mockClassRoomRepo.createNewClassRoom(ArgumentMatchers.any()))
      .thenReturn(Future(if(created) MongoSuccessCreate else MongoFailedCreate))
  }

  def mockGetClassesForTeacher(fetched: Boolean): OngoingStubbing[Future[List[ClassRoom]]] = {
    when(mockClassRoomRepo.getClassesForTeacher(ArgumentMatchers.any()))
      .thenReturn(Future(if(fetched) testClassList else List.empty[ClassRoom]))
  }

  def mockGetClassRoom(fetched: Boolean): OngoingStubbing[Future[Option[ClassRoom]]] = {
    when(mockClassRoomRepo.getClassroom(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(fetched) Some(testClassRoom) else None))
  }

  def mockDeleteClassRoom(deleted: Boolean): OngoingStubbing[Future[MongoDeleteResponse]] = {
    when(mockClassRoomRepo.deleteClassRoom(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(deleted) MongoSuccessDelete else MongoFailedDelete))
  }
}
