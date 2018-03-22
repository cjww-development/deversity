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

case class OrgAccount(orgId: String,
                      deversityId: String,
                      orgName: String,
                      initials: String,
                      orgUserName: String,
                      location: String,
                      orgEmail: String)

object OrgAccount {
  implicit def format(implicit formatters: BaseFormatting): OFormat[OrgAccount] = (
    (__ \ "orgId").format[String] and
    (__ \ "deversityId").format[String] and
    (__ \ "orgName").format[String](formatters.orgNameReads) and
    (__ \ "initials").format[String](formatters.initialsReads) and
    (__ \ "orgUserName").format[String](formatters.orgUserNameReads) and
    (__ \ "location").format[String](formatters.locationReads) and
    (__ \ "orgEmail").format[String](formatters.emailReads)
  )(OrgAccount.apply, unlift(OrgAccount.unapply))
}
