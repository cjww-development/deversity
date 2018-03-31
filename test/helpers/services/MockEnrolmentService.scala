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

import com.cjwwdev.mongo.responses.{MongoFailedUpdate, MongoSuccessUpdate, MongoUpdatedResponse}
import common.MissingAccountException
import helpers.other.Fixtures
import models.{DeversityEnrolment, RegistrationCode}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.EnrolmentService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockEnrolmentService extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockEnrolmentService = mock[EnrolmentService]

  val testDeversityId = generateTestSystemId(DEVERSITY)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockEnrolmentService)
  }

  def mockCreateDeversityId(returned: Future[String]): OngoingStubbing[Future[String]] = {
    when(mockEnrolmentService.createDeversityId(ArgumentMatchers.any()))
      .thenReturn(returned)
  }

  def mockGetDeversityEnrolment(returned: Future[Option[DeversityEnrolment]]): OngoingStubbing[Future[Option[DeversityEnrolment]]] = {
    when(mockEnrolmentService.getEnrolment(ArgumentMatchers.any()))
      .thenReturn(returned)
  }

  def mockUpdateDeversityEnrolment(success: Boolean): OngoingStubbing[Future[MongoUpdatedResponse]] = {
    when(mockEnrolmentService.updateDeversityEnrolment(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future(if(success) MongoSuccessUpdate else MongoFailedUpdate))
  }

  def mockGenerateRegistrationCode(generated: Boolean): OngoingStubbing[Future[Boolean]] = {
    when(mockEnrolmentService.generateRegistrationCode(ArgumentMatchers.any()))
      .thenReturn(Future(generated))
  }

  def mockGetRegistrationCode(fetched: Boolean): OngoingStubbing[Future[RegistrationCode]] = {
    when(mockEnrolmentService.getRegistrationCode(ArgumentMatchers.any()))
      .thenReturn(if(fetched) Future(testRegistrationCode) else Future.failed(new MissingAccountException("")))
  }

  def mockLookupRegistrationCode(regCode: Future[String]): OngoingStubbing[Future[String]] = {
    when(mockEnrolmentService.lookupRegistrationCode(ArgumentMatchers.any()))
      .thenReturn(regCode)
  }
}
