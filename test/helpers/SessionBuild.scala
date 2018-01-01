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

import play.api.test.FakeRequest
import play.api.test.Helpers._

trait SessionBuild {
  def buildRequest(appId: String): FakeRequest[_] = {
    FakeRequest().withHeaders(
      "appId"      -> appId,
      CONTENT_TYPE -> TEXT
    )
  }
}
