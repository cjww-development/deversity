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
import common.MissingAccountException
import models.OrgAccount
import models.formatters.{BaseFormatting, MongoFormatting}
import play.api.Logger
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._
import services.MetricsService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class OrgAccountRepository @Inject()(metricsService: MetricsService) extends MongoDatabase("org-accounts"){

  private implicit val mongoFormatting: BaseFormatting = MongoFormatting

  private def mongoTimer: Timer.Context = metricsService.mongoResponseTimer.time()

  private def getSelectorHead(selector: BSONDocument): (String, String) = (selector.elements.head._1, selector.elements.head._2.toString)

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("orgId" -> IndexType.Ascending),
      name   = Some("OrgId"),
      unique = true,
      sparse = false
    )
  )

  def getSchool(selector: BSONDocument): Future[OrgAccount] = {
    val elements = getSelectorHead(selector)
    metricsService.runMetricsTimer(mongoTimer) {
      collection flatMap {
        _.find(selector).one[OrgAccount] map {
          case Some(acc) => acc
          case _         =>
            Logger.error(s"[OrgAccountRepository] - [getSchool] - No org account found based on ${elements._1} with value ${elements._2}")
            throw new MissingAccountException(s"No org account found based on ${elements._1} with value ${elements._2}")
        }
      }
    }
  }
}
