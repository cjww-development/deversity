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

import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Random.nextString

trait TestDataHelper {

  val USER      = "user"
  val ORG       = "org-user"
  val CONTEXT   = "context"
  val DEVERSITY = "deversity"

  val uuid = UUID.randomUUID()

  def generateTestSystemId(idType: String): String = s"$idType-$uuid"

  val createTestUserName: String = nextString(10)

  val createTestEmail: String = s"${nextString(3)}@${nextString(5)}.com"

  val now = DateTime.now(DateTimeZone.UTC)
}
