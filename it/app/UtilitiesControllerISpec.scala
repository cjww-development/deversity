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
package app

import com.cjwwdev.security.encryption.DataSecurity
import play.api.libs.json.{JsSuccess, JsValue, Json}
import utils.IntegrationStubbing
import play.api.test.Helpers._

class UtilitiesControllerISpec extends IntegrationStubbing {

  s"/utilities/$testOrgId/pending-deversity-enrolments" should {
    "return an Ok" when {
      "the count has been calculated" in {
        
        given
          .user.orgUser.isSetup
          .user.individualUser.isSetup
          .user.orgUser.isAuthorised

        whenReady(client(s"$appUrl/utilities/$testOrgId/pending-deversity-enrolments").get) { res =>
          res.status mustBe OK
          DataSecurity.decryptIntoType[JsValue](res.body) mustBe JsSuccess(Json.parse("""{"pendingCount" : 1}"""))
        }
      }
    }

//    "return an Internal server error" when {
//      "the given org Id cannot be matched against a held account" in {
//        given
//          .user.orgUser.isAuthorised
//
//        whenReady(client(s"$appUrl/utilities/$testOrgId/pending-deversity-enrolments").get) { res =>
//          res.status mustBe INTERNAL_SERVER_ERROR
//        }
//      }
//    }
  }
}
