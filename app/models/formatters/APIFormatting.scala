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

import com.cjwwdev.regex.RegexPack
import play.api.data.validation.ValidationError
import play.api.libs.json.Reads

object APIFormatting extends BaseFormatting with RegexPack {
  override val emailReads: Reads[String] = Reads.StringReads.filter(ValidationError("Invalid email address"))(_.matches(emailRegex.regex))

  override val firstNameReads: Reads[String] = Reads.StringReads.filter(ValidationError("Invalid first name"))(_.matches(firstNameRegex.regex))
  override val lastNameReads: Reads[String]  = Reads.StringReads.filter(ValidationError("Invalid last name"))(_.matches(lastNameRegex.regex))
  override val userNameReads: Reads[String]  = Reads.StringReads.filter(ValidationError("Invalid user name"))(_.matches(userNameRegex.regex))

  override val statusConfirmedReads: Reads[String] =
    Reads.StringReads.filter(ValidationError("Invalid status"))(status => status.equals("pending") || status.equals("confirmed"))
  override val roleReads: Reads[String] =
    Reads.StringReads.filter(ValidationError("Invalid role"))(role => role.equals("teacher") || role.equals("student"))

  override val orgNameReads: Reads[String]     = Reads.StringReads.filter(ValidationError("Invalid org name"))(_.matches(orgNameRegex.regex))
  override val initialsReads: Reads[String]    = Reads.StringReads.filter(ValidationError("Invalid initials"))(_.matches(initialsRegex.regex))
  override val orgUserNameReads: Reads[String] = Reads.StringReads.filter(ValidationError("Invalid org user name"))(_.matches(userNameRegex.regex))
  override val locationReads: Reads[String]    = Reads.StringReads.filter(ValidationError("Invalid location"))(_.matches(locationRegex.regex))
}
