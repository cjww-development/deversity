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
package app

import com.cjwwdev.security.encryption.DataSecurity
import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.implicits.ImplicitJsValues._
import models.ClassRoom
import play.api.libs.json.Writes
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import utils.{IntegrationSpec, IntegrationStubbing}

import scala.concurrent.ExecutionContext.Implicits.global

class ClassRoomAPIISpec extends IntegrationSpec with IntegrationStubbing {

  implicit val stringWrites = Writes.StringWrites

  s"/teacher/$testUserId/create-classroom" should {
    "create a new class room" when {
      "supplied a class room name" in {
        given
          .user.individualUser.isSetup
          .user.individualUser.hasDeversityId
          .user.individualUser.isAuthorised

        val result = await(client(s"$testAppUrl/teacher/$testUserId/create-classroom").post(DataSecurity.encryptString("Test class name")))

        result.status mustBe CREATED

        val classRoom = await(classRoomRepository.collection.flatMap(_.find(BSONDocument("name" -> "Test class name")).one[ClassRoom])).get
        classRoom.name mustBe "Test class name"
      }
    }
  }

  s"/teacher/$testUserId/classrooms" should {
    "fetch all the classes for the teacher" in {
      given
        .user.individualUser.isSetup
        .user.individualUser.hasDeversityId
        .user.individualUser.hasClasses
        .user.individualUser.isAuthorised

      val result = await(client(s"$testAppUrl/teacher/$testUserId/classrooms").get)
      result.status                                                    mustBe OK
      result.json.get[String]("body").decryptIntoType[List[ClassRoom]] mustBe List(ClassRoom(testClassId, testDeversityId, testDeversityId, "Test class name"))
    }
  }

  s"/teacher/$testUserId/classroom/$testClassId (GET)" should {
    "delete the class room" in {
      given
        .user.individualUser.isSetup
        .user.individualUser.hasDeversityId
        .user.individualUser.hasClasses
        .user.individualUser.isAuthorised

      val result = await(client(s"$testAppUrl/teacher/$testUserId/classroom/$testClassId").get)
      result.status                                              mustBe OK
      result.json.get[String]("body").decryptIntoType[ClassRoom] mustBe ClassRoom(testClassId, testDeversityId, testDeversityId, "Test class name")
    }
  }

  s"/teacher/$testUserId/classroom/$testClassId (DELETE)" should {
    "delete the class room" in {
      given
        .user.individualUser.isSetup
        .user.individualUser.hasDeversityId
        .user.individualUser.hasClasses
        .user.individualUser.isAuthorised

      val result = await(client(s"$testAppUrl/teacher/$testUserId/classroom/$testClassId").delete)
      result.status mustBe OK
    }
  }
}
