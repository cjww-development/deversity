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

package helpers.controllers

import com.cjwwdev.http.headers.HeaderPackage
import com.cjwwdev.implicits.ImplicitDataSecurity._
import helpers.auth.{AuthBuilder, MockAuthConnector}
import helpers.other.FutureAsserts
import helpers.services._
import org.scalatestplus.play.PlaySpec
import play.api.http.{HeaderNames, HttpProtocol, MimeTypes, Status}
import play.api.test._

trait ControllerSpec
  extends PlaySpec
    with FutureAsserts
    with AuthBuilder
    with MockAuthConnector
    with MockClassRoomService
    with MockEnrolmentService
    with MockUtilitiesService
    with MockValidationService
    with HeaderNames
    with Status
    with MimeTypes
    with HttpProtocol
    with ResultExtractors
    with Writeables
    with EssentialActionCaller
    with RouteInvokers {

  val testSessionId = generateTestSystemId(SESSION)
  val testUserId    = generateTestSystemId(USER)

  val standardRequest = FakeRequest().withHeaders(
    "cjww-headers" -> HeaderPackage("abda73f4-9d52-4bb8-b20d-b5fffd0cc130", Some(testSessionId)).encrypt,
    CONTENT_TYPE   -> TEXT
  )
}
