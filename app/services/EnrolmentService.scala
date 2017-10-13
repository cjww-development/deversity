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

import javax.inject.{Inject, Singleton}

import com.cjwwdev.reactivemongo.MongoUpdatedResponse
import com.cjwwdev.security.encryption.DataSecurity
import models.DeversityEnrolment
import repositories.UserAccountRepository
import services.selectors.UserAccountSelectors.userIdSelector

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class EnrolmentService @Inject()(userAccountRepository: UserAccountRepository) {
  def createDeversityId(userId: String): Future[String] = {
    userAccountRepository.createDeversityId(userId) map DataSecurity.encryptString
  }

  def getEnrolment(userId: String): Future[Option[DeversityEnrolment]] = {
    userAccountRepository.getUserBySelector(userIdSelector(userId)) map(_.deversityDetails)
  }

  def updateDeversityEnrolment(userId: String, deversityEnrolment: DeversityEnrolment): Future[MongoUpdatedResponse] = {
    userAccountRepository.updateDeversityEnrolment(userId, deversityEnrolment)
  }
}
