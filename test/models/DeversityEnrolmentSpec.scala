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

import com.cjwwdev.testing.common.JsonValidation
import models.formatters.{APIFormatting, BaseFormatting, MongoFormatting}
import org.scalatestplus.play.PlaySpec
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class DeversityEnrolmentSpec extends PlaySpec with JsonValidation {
  "DeversityEnrolment" should {
    "be read into json" when {
      "using the MongoFormatter" in {
        implicit val mongoFormatter: BaseFormatting = MongoFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val testModel = DeversityEnrolment(
          statusConfirmed = "pending",
          schoolName = "TestSchoolName",
          role = "teacher",
          title = Some("Professor"),
          room = Some("testRoom"),
          teacher = None
        )

        Json.fromJson[DeversityEnrolment](testJson) mustBe JsSuccess(testModel)
      }

      "using the MongoFormatter and fail" in {
        implicit val mongoFormatter: BaseFormatting = MongoFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : 1,
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher",
            | "title" : "Professor",
            | "room" : true
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)
        result.getClass mustBe classOf[JsError]
      }

      "using the APIFormatter" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val testModel = DeversityEnrolment(
          statusConfirmed = "pending",
          schoolName = "TestSchoolName",
          role = "teacher",
          title = Some("Professor"),
          room = Some("testRoom"),
          teacher = None
        )

        Json.fromJson[DeversityEnrolment](testJson) mustBe JsSuccess(testModel)
      }

      "using the APIFormatter and fail" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "invalidStatus",
            | "schoolName" : "TestSchoolName",
            | "role" : "invalidRole",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() \ "statusConfirmed" -> Seq(ValidationError("Invalid status")),
          JsPath() \ "role"            -> Seq(ValidationError("Invalid role"))
        ))
      }
    }

    "fail reads" when {
      "the role is teacher but title isn't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath()  -> Seq(ValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }

      "the role is teacher but room isn't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher",
            | "title" : "Professor"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath()  -> Seq(ValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }

      "the role is teacher but title and room aren't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() -> Seq(ValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }

      "the role is teacher but title and room aren't defined and teacher is defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "teacher",
            | "teacher" : "testTeacher"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() -> Seq(ValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }


      "the role is student but teacher isn't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "student"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath()  -> Seq(ValidationError("Role was student but teacher wasn't defined"))
        ))
      }

      "the role is student but teacher isn't defined and title and room are" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "statusConfirmed" : "pending",
            | "schoolName" : "TestSchoolName",
            | "role" : "student",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() -> Seq(ValidationError("Role was student but teacher wasn't defined"))
        ))
      }
    }
  }
}
