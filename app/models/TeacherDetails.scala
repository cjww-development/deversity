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

case class TeacherDetails(userId: String,
                          title: String,
                          lastName: String,
                          room: String)

object TeacherDetails {

  def reads(formatters: BaseFormatting): Reads[TeacherDetails] = (
    (__ \ "userId").read[String] and
    (__ \ "title").read[String] and
    (__ \ "lastName").read[String](formatters.lastNameReads) and
    (__ \ "room").read[String]
  )(TeacherDetails.apply _)

  val writes: OWrites[TeacherDetails] = (
    (__ \ "userId").write[String] and
    (__ \ "title").write[String] and
    (__ \ "lastName").write[String] and
    (__ \ "room").write[String]
  )(unlift(TeacherDetails.unapply))

  implicit def format(implicit formatters: BaseFormatting): Format[TeacherDetails] = OFormat(reads(formatters), writes)

  implicit val obfuscator: Obfuscator[TeacherDetails] = new Obfuscator[TeacherDetails] {
    override def encrypt(value: TeacherDetails): String = Obfuscation.obfuscateJson(Json.toJson(value)(writes))
  }
}
