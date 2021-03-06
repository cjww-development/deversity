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

package repositories

import com.cjwwdev.logging.Logging
import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import common.MissingAccountException
import javax.inject.Inject
import models.OrgAccount
import play.api.Configuration
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultOrgAccountRepository @Inject()(val config: Configuration) extends OrgAccountRepository with ConnectionSettings

trait OrgAccountRepository extends DatabaseRepository with Logging {

  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("orgId" -> IndexType.Ascending),
      name   = Some("OrgId"),
      unique = true,
      sparse = false
    )
  )

  def getSchool(selector: BSONDocument)(implicit ec: ExC): Future[OrgAccount] = {
    for {
      col          <- collection
      (key, value) =  getSelectorHead(selector)
      acc          <- col.find(selector).one[OrgAccount]
    } yield acc.getOrElse {
      logger.error(s"[getSchool] - No org account found based on $key with value $value")
      throw new MissingAccountException(s"No org account found based on $key with value $value")
    }
  }
}

