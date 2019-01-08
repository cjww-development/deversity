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

import com.cjwwdev.mongo.DatabaseRepository
import com.cjwwdev.mongo.connection.ConnectionSettings
import common.{RegistrationCodeExpiredException, RegistrationCodeNotFoundException}
import javax.inject.Inject
import models.RegistrationCode
import org.joda.time.{DateTime, Interval}
import play.api.Configuration
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json._

import scala.concurrent.{Future, ExecutionContext => ExC}

class DefaultRegistrationCodeRepository @Inject()(val config: Configuration) extends RegistrationCodeRepository with ConnectionSettings

trait RegistrationCodeRepository extends DatabaseRepository {
  override def indexes: Seq[Index] = Seq(
    Index(
      key    = Seq("identifier" -> IndexType.Ascending),
      name   = Some("Identifier"),
      unique = true,
      sparse = false
    ),
    Index(
      key    = Seq("code" -> IndexType.Ascending),
      name   = Some("RegCode"),
      unique = true,
      sparse = false
    )
  )

  private def buildUpdate(regCode: String): BSONDocument = BSONDocument(
    "$set" -> BSONDocument(
      "code"      -> regCode,
      "createdAt" -> BSONDocument(
        "$date" -> DateTime.now.getMillis
      )
    )
  )

  private def getInterval(time: DateTime): Long = new Interval(time, DateTime.now).toDuration.getStandardMinutes

  def getRegistrationCode(userId: String, regCode: String)(implicit ec: ExC): Future[RegistrationCode] = {
    for {
      col           <- collection
      regCodeRecord <- col.find(BSONDocument("identifier" -> userId)).one[RegistrationCode]
      regCode       <- regCodeRecord match {
        case Some(code) => Future.successful(code)
        case None       =>
          val code = RegistrationCode(userId, regCode, DateTime.now)
          col.insert[RegistrationCode](code) map(_ => code)
      }
    } yield regCode
  }

  def generateRegistrationCode(userId: String, regCode: String)(implicit ec: ExC): Future[UpdateWriteResult] = {
    for {
      col         <- collection
      updatedCode =  buildUpdate(regCode)
      update      <- col.update(BSONDocument("identifier" -> userId), updatedCode)
    } yield update
  }

  def lookupUserIdByRegCode(code: String)(implicit ec: ExC): Future[String] = {
    for {
      col        <- collection
      regCode    <- col.find(BSONDocument("code" -> code)).one[RegistrationCode]
    } yield regCode.fold(throw new RegistrationCodeNotFoundException(s"Registration code $regCode not found")) { x =>
      if(getInterval(x.createdAt) < 15) x.identifier else throw new RegistrationCodeExpiredException(s"Registration code for ${x.identifier} has expired")
    }
  }
}
