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

import javax.inject.{Inject, Singleton}

import com.cjwwdev.reactivemongo.MongoDatabase
import com.codahale.metrics.Timer
import models.OrgAccount
import models.formatters.{BaseFormatting, MongoFormatting}
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import services.MetricsService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class OrgAccountRepository @Inject()(metricsService: MetricsService) extends MongoDatabase("org-accounts"){

  implicit val mongoFormatting: BaseFormatting = MongoFormatting

  def mongoTimer: Timer.Context = metricsService.mongoResponseTimer.time()

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("orgId" -> IndexType.Ascending),
      name   = Some("OrgId"),
      unique = true,
      sparse = false
    )
  )

  private def orgUserNameSelector(orgUserName: String): BSONDocument = BSONDocument("orgUserName" -> orgUserName)
  private def orgIdSelector(orgId: String): BSONDocument = BSONDocument("orgId" -> orgId)

  def getSchoolById(orgId: String): Future[Option[OrgAccount]] = {
    metricsService.runMetricsTimer(mongoTimer) {
      collection flatMap(_.find(orgIdSelector(orgId)).one[OrgAccount])
    }
  }

  def getSchoolByUserName(orgUserName: String): Future[Option[OrgAccount]] = {
    metricsService.runMetricsTimer(mongoTimer) {
      collection flatMap(_.find(orgUserNameSelector(orgUserName)).one[OrgAccount])
    }
  }
}
