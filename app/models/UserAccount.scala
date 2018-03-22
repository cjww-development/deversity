/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models

import models.formatters.BaseFormatting
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class UserAccount(userId: String,
                       firstName: String,
                       lastName: String,
                       userName: String,
                       email: String,
                       deversityDetails: Option[DeversityEnrolment],
                       enrolments: Option[JsObject])

object UserAccount {
  implicit def format(implicit formatters: BaseFormatting): OFormat[UserAccount] = (
    (__ \ "userId").format[String] and
    (__ \ "firstName").format[String](formatters.firstNameReads) and
    (__ \ "lastName").format[String](formatters.lastNameReads) and
    (__ \ "userName").format[String](formatters.userNameReads) and
    (__ \ "email").format[String](formatters.emailReads) and
    (__ \ "deversityDetails").formatNullable[DeversityEnrolment](DeversityEnrolment.format) and
    (__ \ "enrolments").formatNullable[JsObject]
  )(UserAccount.apply, unlift(UserAccount.unapply))
}
