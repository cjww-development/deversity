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
package common

import javax.inject.Inject

import com.cjwwdev.filters.RequestLoggingFilter
import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.mongo.indexes.RepositoryIndexer
import com.cjwwdev.request.RequestParsers
import com.kenshoo.play.metrics.MetricsFilter
import models.formatters.{APIFormatting, BaseFormatting}
import play.api.http.DefaultHttpFilters
import play.api.mvc.Controller
import repositories.{ClassRoomRepository, OrgAccountRepository, RegistrationCodeRepository, UserAccountRepository}

trait BackendController extends Controller with RequestParsers with IdentifierValidation {
  implicit val formatter: BaseFormatting = APIFormatting
}

class EnabledFilters @Inject()(loggingFilter: RequestLoggingFilter, metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(loggingFilter, metricsFilter)

class RepositoryIndexerImpl @Inject()(classRoomRepository: ClassRoomRepository,
                                      orgAccountRepository: OrgAccountRepository,
                                      registrationCodeRepository: RegistrationCodeRepository,
                                      userAccountRepository: UserAccountRepository) extends RepositoryIndexer {
  override val repositories = Seq(
    classRoomRepository,
    orgAccountRepository,
    registrationCodeRepository,
    userAccountRepository
  )
  runIndexing
}

class MissingAccountException(msg: String) extends Exception(msg)
class UpdateFailedException(msg: String) extends Exception(msg)
class AlreadyExistsException(msg: String) extends Exception(msg)

class RegistrationCodeExpiredException(msg: String) extends Exception(msg)
class RegistrationCodeNotFoundException(msg: String) extends Exception(msg)

class EnrolmentsNotFoundException(msg: String) extends Exception(msg)

class DeversityIdNotFoundException(msg: String) extends Exception(msg)
