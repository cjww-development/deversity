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
package models.formatters

import play.api.libs.json._

object MongoFormatting extends BaseFormatting {
  def standardStringRead: Reads[String] = new Reads[String] {
    override def reads(json: JsValue): JsResult[String] = json match {
      case JsString(str)  => JsSuccess(str)
      case _              => JsError()
    }
  }

  override val emailReads = standardStringRead

  override val firstNameReads = standardStringRead
  override val lastNameReads  = standardStringRead
  override val userNameReads  = standardStringRead

  override val statusConfirmedReads = standardStringRead
  override val roleReads            = standardStringRead

  override val orgNameReads     = standardStringRead
  override val initialsReads    = standardStringRead
  override val orgUserNameReads = standardStringRead
  override val locationReads    = standardStringRead
}
