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

import javax.inject.Inject

import com.cjwwdev.config.ConfigurationLoader
import com.cjwwdev.reactivemongo.MongoDatabase
import config.MissingAccountException
import models.OrgAccount
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OrgAccountRepositoryImpl @Inject()(val configurationLoader: ConfigurationLoader) extends OrgAccountRepository

trait OrgAccountRepository extends MongoDatabase {

  //private def mongoTimer: Timer.Context = metricsService.mongoResponseTimer.time()

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("orgId" -> IndexType.Ascending),
      name   = Some("OrgId"),
      unique = true,
      sparse = false
    )
  )

  def getSchool(selector: BSONDocument): Future[OrgAccount] = {
    for {
      col      <- collection
      elements =  getSelectorHead(selector)
      acc      <- col.find(selector).one[OrgAccount]
    } yield acc.getOrElse {
      logger.error(s"[getSchool] - No org account found based on ${elements._1} with value ${elements._2}")
      throw new MissingAccountException(s"No org account found based on ${elements._1} with value ${elements._2}")
    }
  }
}

