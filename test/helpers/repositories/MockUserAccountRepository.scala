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

import com.cjwwdev.mongo.responses.{MongoSuccessUpdate, MongoUpdatedResponse}
import common.UpdateFailedException
import helpers.other.Fixtures
import models.UserAccount
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.OngoingStubbing
import repositories.UserAccountRepository

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockUserAccountRepository extends BeforeAndAfterEach with MockitoSugar with Fixtures{
  self: PlaySpec =>

  val mockUserAccountRepo = mock[UserAccountRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUserAccountRepo)
  }

  def mockGetUserBySelector(returned: UserAccount): OngoingStubbing[Future[UserAccount]] = {
    when(mockUserAccountRepo.getUserBySelector(ArgumentMatchers.any()))
      .thenReturn(Future(returned))
  }

  def mockCreateDeversityId(deversityId: String): OngoingStubbing[Future[String]] = {
    when(mockUserAccountRepo.createDeversityId(ArgumentMatchers.any()))
      .thenReturn(Future(deversityId))
  }

  def mockUpdateDeversityEnrolment(updated: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockUserAccountRepo.updateDeversityEnrolment(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(if(updated) Future(MongoSuccessUpdate) else Future.failed(new UpdateFailedException("")))
  }
}
