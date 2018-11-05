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
import play.api.libs.json.{Json, OFormat}

case class ClassRoom(classId: String,
                     schooldevId: String,
                     teacherDevId: String,
                     name: String)

object ClassRoom {
  implicit val format: OFormat[ClassRoom] = Json.format[ClassRoom]

  implicit val listObfuscator: Obfuscator[List[ClassRoom]] = new Obfuscator[List[ClassRoom]] {
    override def encrypt(value: List[ClassRoom]): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }

  implicit val obfuscator: Obfuscator[ClassRoom] = new Obfuscator[ClassRoom] {
    override def encrypt(value: ClassRoom): String = Obfuscation.obfuscateJson(Json.toJson(value))
  }
}
