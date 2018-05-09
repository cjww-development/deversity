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

import common.MissingAccountException
import helpers.other.Fixtures
import models.{OrgDetails, TeacherDetails}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import services.UtilitiesService
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockUtilitiesService extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockUtilitiesService = mock[UtilitiesService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockUtilitiesService)
  }

  def mockGetSchoolDetails(fetched: Boolean): OngoingStubbing[Future[OrgDetails]] = {
    when(mockUtilitiesService.getSchoolDetails(ArgumentMatchers.any()))
      .thenReturn(if(fetched) Future(testOrgDetails) else Future.failed(new MissingAccountException("")))
  }

  def mockGetTeacherDetails(fetched: Boolean): OngoingStubbing[Future[TeacherDetails]] = {
    when(mockUtilitiesService.getTeacherDetails(ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(if(fetched) Future(testTeacherDetails) else Future.failed(new MissingAccountException("")))
  }
}
