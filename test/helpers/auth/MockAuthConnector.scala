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

package helpers.auth

import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.auth.models.CurrentUser
import helpers.other.Fixtures
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MockAuthConnector extends BeforeAndAfterEach with MockitoSugar with Fixtures {
  self: PlaySpec =>

  val mockAuthConnector = mock[AuthConnector]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector)
  }

  def mockGetIndCurrentUser(fetched: Boolean): OngoingStubbing[Future[Option[CurrentUser]]] = {
    when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any()))
      .thenReturn(Future(if(fetched) Some(testCurrentUser) else None))
  }

  def mockGetOrgCurrentUser(fetched: Boolean): OngoingStubbing[Future[Option[CurrentUser]]] = {
    when(mockAuthConnector.getCurrentUser(ArgumentMatchers.any()))
      .thenReturn(Future(if(fetched) Some(testOrgCurrentUser) else None))
  }
}
