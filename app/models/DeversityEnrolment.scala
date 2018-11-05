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

import com.cjwwdev.security.deobfuscation.{DeObfuscation, DeObfuscator, DecryptionError}
import com.cjwwdev.security.obfuscation.{Obfuscation, Obfuscator}
import models.formatters.BaseFormatting
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.reflect.ClassTag

case class DeversityEnrolment(schoolDevId: String,
                              role: String,
                              title: Option[String],
                              room: Option[String],
                              teacher: Option[String])

object DeversityEnrolment {
  implicit def format(implicit formatters: BaseFormatting): Format[DeversityEnrolment] = OFormat(reads(formatters), writes)

  def reads(implicit formatters: BaseFormatting): Reads[DeversityEnrolment] = (
    (__ \ "schoolDevId").read[String] and
    (__ \ "role").read[String](formatters.roleReads) and
    (__ \ "title").readNullable[String] and
    (__ \ "room").readNullable[String] and
    (__ \ "teacher").readNullable[String]
  )(DeversityEnrolment.apply _).filterNot(JsonValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))(
    enr => enr.role == "teacher" && (enr.title.isEmpty || enr.room.isEmpty || enr.teacher.isDefined)
  ).filterNot(JsonValidationError("Role was student but teacher wasn't defined"))(
    enr => enr.role == "student" && (enr.title.isDefined || enr.room.isDefined || enr.teacher.isEmpty)
  )

  def writes: OWrites[DeversityEnrolment] = (
    (__ \ "schoolDevId").write[String] and
    (__ \ "role").write[String] and
    (__ \ "title").writeNullable[String] and
    (__ \ "room").writeNullable[String] and
    (__ \ "teacher").writeNullable[String]
  )(unlift(DeversityEnrolment.unapply))

  implicit val obfuscator: Obfuscator[DeversityEnrolment] = new Obfuscator[DeversityEnrolment] {
    override def encrypt(value: DeversityEnrolment): String = Obfuscation.obfuscateJson(Json.toJson(value)(writes))
  }

  implicit def deObfuscator(implicit formatter: BaseFormatting, tag: ClassTag[DeversityEnrolment]): DeObfuscator[DeversityEnrolment] = {
    new DeObfuscator[DeversityEnrolment] {
      override def decrypt(value: String): Either[DeversityEnrolment, DecryptionError] = {
        DeObfuscation.deObfuscate(value)(reads, tag)
      }
    }
  }
}
