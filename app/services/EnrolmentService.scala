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
package services

import com.cjwwdev.implicits.ImplicitDataSecurity._
import com.cjwwdev.mongo.responses.MongoUpdatedResponse
import com.cjwwdev.security.obfuscation.Obfuscation._
import javax.inject.Inject
import models.{DeversityEnrolment, RegistrationCode}
import repositories.{RegistrationCodeRepository, UserAccountRepository}
import selectors.UserAccountSelectors.userIdSelector

import scala.concurrent.{Future, ExecutionContext => ExC}
import scala.util.Random

class DefaultEnrolmentService @Inject()(val userAccountRepository: UserAccountRepository,
                                        val registrationCodeRepository: RegistrationCodeRepository) extends EnrolmentService

trait EnrolmentService {
  val userAccountRepository: UserAccountRepository
  val registrationCodeRepository: RegistrationCodeRepository

  private val regCodeLength       = 6
  private val regCodeAllowedChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  def createDeversityId(userId: String)(implicit ec: ExC): Future[String] = {
    userAccountRepository.createDeversityId(userId) map(_.encrypt)
  }

  def getEnrolment(userId: String)(implicit ec: ExC): Future[Option[DeversityEnrolment]] = {
    userAccountRepository.getUserBySelector(userIdSelector(userId)) map(_.deversityDetails)
  }

  def updateDeversityEnrolment(userId: String, deversityEnrolment: DeversityEnrolment)(implicit ec: ExC): Future[MongoUpdatedResponse] = {
    userAccountRepository.updateDeversityEnrolment(userId, deversityEnrolment)
  }

  private def generateRegistrationCode: String = {
    val regCode = new StringBuilder
    for(_ <- 1 to regCodeLength) {
      val randomNum = Random.nextInt(regCodeAllowedChars.length)
      regCode.append(regCodeAllowedChars(randomNum))
    }
    regCode.toString
  }

  def getRegistrationCode(userId: String)(implicit ec: ExC): Future[RegistrationCode] = {
    registrationCodeRepository.getRegistrationCode(userId, generateRegistrationCode)
  }

  def generateRegistrationCode(userId: String)(implicit ec: ExC): Future[Boolean] = {
    registrationCodeRepository.generateRegistrationCode(userId, generateRegistrationCode) map(_.ok)
  }

  def lookupRegistrationCode(regCode: String)(implicit ec: ExC): Future[String] = {
    registrationCodeRepository.lookupUserIdByRegCode(regCode)
  }
}
