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
package repositories

import java.util.UUID
import javax.inject.{Inject, Singleton}

import com.cjwwdev.reactivemongo.{MongoDatabase, MongoSuccessUpdate, MongoUpdatedResponse}
import com.codahale.metrics.Timer
import common.{AlreadyExistsException, MissingAccountException, UpdateFailedException}
import models.{DeversityEnrolment, UserAccount}
import models.formatters.{BaseFormatting, MongoFormatting}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import services.MetricsService
import services.selectors.UserAccountSelectors.userIdSelector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UserAccountRepository @Inject()(metricsService: MetricsService) extends MongoDatabase("user-accounts") {

  private implicit val mongoFormatting: BaseFormatting = MongoFormatting

  private def mongoTimer: Timer.Context = metricsService.mongoResponseTimer.time()

  private def generateDeversityId: String = s"deversity-${UUID.randomUUID()}"

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("userId" -> IndexType.Ascending),
      name   = Some("UserId"),
      unique = true,
      sparse = false
    )
  )

  private def getSelectorHead(selector: BSONDocument): (String, String) = (selector.elements.head._1, selector.elements.head._2.toString)

  def getUserBySelector(selector: BSONDocument): Future[UserAccount] = {
    val elements = getSelectorHead(selector)
    collection flatMap {
      _.find(selector).one[UserAccount] map {
        case Some(acc) => acc
        case _         =>
          Logger.error(s"[UserAccountRepository] - [getUserBySelector] - Could not find user account based on ${elements._1} with value ${elements._2}")
          throw new MissingAccountException(s"No user account found based on ${elements._1} with value ${elements._2}")
      }
    }
  }

  def getPendingEnrolmentCount(orgName: String): Future[Int] = {
    val query = BSONDocument(
      "deversityDetails.schoolName"      -> orgName,
      "deversityDetails.role"            -> "teacher",
      "deversityDetails.statusConfirmed" -> "pending"
    )

    metricsService.runMetricsTimer(mongoTimer) {
      collection flatMap {
        _.find(query).cursor[UserAccount]().collect[List]() map(_.size)
      }
    }
  }

  def createDeversityId(userId: String): Future[String] = {
    val deversityId   = generateDeversityId
    val findQuery     = BSONDocument("userId" -> userId, "enrolments.deversityId" -> BSONDocument("$exists" -> true))
    val enrolment     = BSONDocument("$set" -> BSONDocument("enrolments.deversityId" -> deversityId))
    for {
      col       <- collection
      accExists <- col.find(userIdSelector(userId)).one[UserAccount] map(acc => acc.isDefined)
      idExists  <- if(accExists) {
        col.find(findQuery).one[UserAccount] map(acc => acc.isDefined)
      } else {
        Logger.error(s"[UserAccountRepository] - [createDeversityId] - No user account found for user id $userId")
        throw new MissingAccountException(s"No user account found for user id $userId")
      }
      updated   <- if(!idExists) {
        col.update(userIdSelector(userId), enrolment) map(_ => deversityId)
      } else {
        Logger.warn(s"[UserAccountRepository] - [createDeversityId] - Deversity id for user $userId already exists")
        throw new AlreadyExistsException(s"Deversity id for user $userId already exists")
      }
    } yield updated
  }

  def updateDeversityEnrolment(userId: String, deversityEnrolment: DeversityEnrolment): Future[MongoUpdatedResponse] = {
    val enrolmentUpdate = BSONDocument("$set" -> BSONDocument("deversityDetails" -> Json.toJson(deversityEnrolment).as[JsObject]))
    collection flatMap {
      _.update(userIdSelector(userId), enrolmentUpdate) map { wr =>
        if(wr.ok) MongoSuccessUpdate else throw new UpdateFailedException(s"There was a problem updating the deversity enrolment for user $userId")
      }
    }
  }
}
