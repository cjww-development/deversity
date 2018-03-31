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

import com.cjwwdev.testing.unit.helpers.FakeMongoResults
import helpers.other.Fixtures
import models.RegistrationCode
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import repositories.RegistrationCodeRepository
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.OngoingStubbing
import reactivemongo.api.commands.UpdateWriteResult

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockRegCodeRepository extends BeforeAndAfterEach with MockitoSugar with Fixtures with FakeMongoResults {
  self: PlaySpec =>

  val mockRegCodeRepo = mock[RegistrationCodeRepository]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRegCodeRepo)
  }

  def mockGetRegistrationCode(regCode: RegistrationCode): OngoingStubbing[Future[RegistrationCode]] = {
    when(mockRegCodeRepo.getRegistrationCode(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(regCode))
  }

  def mockGenerateRegistrationCode(generated: Boolean): OngoingStubbing[Future[UpdateWriteResult]] = {
    when(mockRegCodeRepo.generateRegistrationCode(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(if(generated) Future(fakeSuccessUpdateWriteResult) else Future(fakeFailedUpdateWriteResult))
  }

  def mockLookupUserIdByRegCode(userId: String): OngoingStubbing[Future[String]] = {
    when(mockRegCodeRepo.lookupUserIdByRegCode(ArgumentMatchers.any()))
      .thenReturn(Future(userId))
  }
}
