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

import com.cjwwdev.security.obfuscation.{Obfuscation, Obfuscator}
import models.formatters.BaseFormatting
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class OrgDetails(orgName: String, initials: String, location: String)

object OrgDetails {

  implicit def format(implicit formatters: BaseFormatting): Format[OrgDetails] = OFormat(reads(formatters), writes)

  def reads(formatters: BaseFormatting): Reads[OrgDetails] = (
    (__ \ "orgName").read[String](formatters.orgNameReads) and
    (__ \ "initials").read[String](formatters.initialsReads) and
    (__ \ "location").read[String](formatters.locationReads)
  )(OrgDetails.apply _)

  val writes: OWrites[OrgDetails] = (
    (__ \ "orgName").write[String] and
    (__ \ "initials").write[String] and
    (__ \ "location").write[String]
  )(unlift(OrgDetails.unapply))

  implicit val obfuscator: Obfuscator[OrgDetails] = new Obfuscator[OrgDetails] {
    override def encrypt(value: OrgDetails): String = Obfuscation.obfuscateJson(Json.toJson(value)(writes))
  }
}
