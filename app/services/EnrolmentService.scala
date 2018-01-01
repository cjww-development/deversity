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
package services

import javax.inject.Inject

import com.cjwwdev.reactivemongo.MongoUpdatedResponse
import com.cjwwdev.security.encryption.DataSecurity
import models.{DeversityEnrolment, RegistrationCode}
import repositories.{OrgAccountRepository, RegistrationCodeRepository, UserAccountRepository}
import services.selectors.UserAccountSelectors.userIdSelector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Random

class EnrolmentServiceImpl @Inject()(val userAccountRepository: UserAccountRepository,
                                     val orgAccountRepository: OrgAccountRepository,
                                     val registrationCodeRepository: RegistrationCodeRepository) extends EnrolmentService

trait EnrolmentService {
  val userAccountRepository: UserAccountRepository
  val orgAccountRepository: OrgAccountRepository
  val registrationCodeRepository: RegistrationCodeRepository

  private val regCodeLength       = 6
  private val regCodeAllowedChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  def createDeversityId(userId: String): Future[String] = {
    userAccountRepository.createDeversityId(userId) map DataSecurity.encryptString
  }

  def getEnrolment(userId: String): Future[Option[DeversityEnrolment]] = {
    userAccountRepository.getUserBySelector(userIdSelector(userId)) map(_.deversityDetails)
  }

  def updateDeversityEnrolment(userId: String, deversityEnrolment: DeversityEnrolment): Future[MongoUpdatedResponse] = {
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

  def getRegistrationCode(userId: String): Future[RegistrationCode] = {
    registrationCodeRepository.getRegistrationCode(userId, generateRegistrationCode)
  }

  def generateRegistrationCode(userId: String): Future[Boolean] = {
    registrationCodeRepository.generateRegistrationCode(userId, generateRegistrationCode) map(_.ok)
  }

  def lookupRegistrationCode(regCode: String): Future[String] = {
    registrationCodeRepository.lookupUserIdByRegCode(regCode)
  }
}
