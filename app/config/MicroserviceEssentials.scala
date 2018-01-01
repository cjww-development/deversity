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
package config

import javax.inject.Inject

import com.cjwwdev.filters.RequestLoggingFilter
import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.request.RequestParsers
import com.kenshoo.play.metrics.MetricsFilter
import models.formatters.{APIFormatting, BaseFormatting}
import org.slf4j.{Logger, LoggerFactory}
import play.api.http.DefaultHttpFilters
import play.api.mvc.Controller

trait BackendController extends Controller with RequestParsers with IdentifierValidation with Logging {
  implicit val formatter: BaseFormatting = APIFormatting
}

trait Logging {
  val logger: Logger = LoggerFactory.getLogger(getClass)
}

class EnabledFilters @Inject()(loggingFilter: RequestLoggingFilter, metricsFilter: MetricsFilter)
  extends DefaultHttpFilters(loggingFilter, metricsFilter)

class MissingAccountException(msg: String) extends Exception(msg)
class UpdateFailedException(msg: String) extends Exception(msg)
class AlreadyExistsException(msg: String) extends Exception(msg)

class RegistrationCodeExpiredException(msg: String) extends Exception(msg)
class RegistrationCodeNotFoundException(msg: String) extends Exception(msg)

class EnrolmentsNotFoundException(msg: String) extends Exception(msg)
