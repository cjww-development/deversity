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

import models.formatters.{APIFormatting, BaseFormatting, MongoFormatting}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class DeversityEnrolmentSpec extends PlaySpec {
  implicit class JsonValidationOps[T](jsResult: JsResult[T]) {
    def mustHaveErrors(expectedErrors: Map[JsPath, Seq[JsonValidationError]]): Unit = jsResult match {
      case JsSuccess(data, _) => fail(s"read should have failed and didn't - reads produced $data")
      case JsError(errors)    => for((path, errs) <- errors) {
        expectedErrors.keySet must   contain(path)
        expectedErrors(path)  mustBe errs
      }
    }
  }

  "DeversityEnrolment" should {
    "be read into json" when {
      "using the MongoFormatter" in {
        implicit val mongoFormatter: BaseFormatting = MongoFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "teacher",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val testModel = DeversityEnrolment(
          schoolDevId = "testOrgDevId",
          role        = "teacher",
          title       = Some("Professor"),
          room        = Some("testRoom"),
          teacher     = None
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
            | "schoolDevId" : "testOrgDevId",
            | "role" : "teacher",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val testModel = DeversityEnrolment(
          schoolDevId = "testOrgDevId",
          role        = "teacher",
          title       = Some("Professor"),
          room        = Some("testRoom"),
          teacher     = None
        )

        Json.fromJson[DeversityEnrolment](testJson) mustBe JsSuccess(testModel)
      }

      "using the APIFormatter and fail" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "invalidRole",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() \ "role" -> Seq(JsonValidationError("Invalid role"))
        ))
      }
    }

    "fail reads" when {
      "the role is teacher but title isn't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "teacher",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath()  -> Seq(JsonValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }

      "the role is teacher but room isn't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "teacher",
            | "title" : "Professor"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath()  -> Seq(JsonValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }

      "the role is teacher but title and room aren't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "teacher"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() -> Seq(JsonValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }

      "the role is teacher but title and room aren't defined and teacher is defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "teacher",
            | "teacher" : "testTeacher"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() -> Seq(JsonValidationError("Role was teacher but either title or room were not defined or teacher was defined and shouldn't"))
        ))
      }


      "the role is student but teacher isn't defined" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "student"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath()  -> Seq(JsonValidationError("Role was student but teacher wasn't defined"))
        ))
      }

      "the role is student but teacher isn't defined and title and room are" in {
        implicit val apiFormatter: BaseFormatting = APIFormatting

        val testJson = Json.parse(
          """
            |{
            | "schoolDevId" : "testOrgDevId",
            | "role" : "student",
            | "title" : "Professor",
            | "room" : "testRoom"
            |}
          """.stripMargin
        )

        val result = Json.fromJson[DeversityEnrolment](testJson)

        result.mustHaveErrors(Map(
          JsPath() -> Seq(JsonValidationError("Role was student but teacher wasn't defined"))
        ))
      }
    }
  }
}
