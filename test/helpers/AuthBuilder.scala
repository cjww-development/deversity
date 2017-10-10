// Copyright (C) 2016-2017 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package helpers

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.cjwwdev.auth.connectors.AuthConnector
import com.cjwwdev.auth.models.{AuthContext, User}
import org.joda.time.{DateTime, DateTimeZone}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc.{Action, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

object AuthBuilder extends SessionBuild {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  final val now = new DateTime(DateTimeZone.UTC)

  def testContext(accountType: String, uuid: UUID): AuthContext = {
    accountType match {
      case "user" => AuthContext(
        contextId = s"context-$uuid",
        user = User(s"user-$uuid", Some("testFirstName"), Some("testLastName"), None, "individual", None),
        basicDetailsUri = "testLink",
        enrolmentsUri   = "testLink",
        settingsUri     = "testLink",
        createdAt       = now
      )
      case "org" => AuthContext(
        contextId = s"context-$uuid",
        user = User(s"org-user-$uuid", None, None, Some("testOrgName"), "organisation", None),
        basicDetailsUri  = "testLink",
        enrolmentsUri    = "testLink",
        settingsUri      = "testLink",
        createdAt        = now
      )
    }
  }

  def getWithAuthorisedUser[T](action: Action[T], request: FakeRequest[_],
                               mockAuthConnector: AuthConnector, uuid: UUID,
                               orgType: String)(test: Future[Result] => Any): Any = {

    val context = testContext(orgType, uuid)

    when(mockAuthConnector.getContext(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(context)))

    val result = action.apply(request).run()
    test(result)
  }

  def postWithAuthorisedUser(action: Action[String], request: FakeRequest[String],
                              mockAuthConnector: AuthConnector, uuid: UUID,
                              orgType: String)(test: Future[Result] => Any): Any = {

    val context = testContext(orgType, uuid)

    when(mockAuthConnector.getContext(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Some(context)))

    val result = action.apply(request)
    test(result)
  }
}