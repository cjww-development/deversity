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

import com.cjwwdev.config.{ConfigurationLoader, ConfigurationLoaderImpl}
import com.google.inject.AbstractModule
import controllers._
import repositories._
import services._

class ServiceBindings extends AbstractModule {
  override def configure(): Unit = {
    bindOther()
    bindRepositories()
    bindServices()
    bindControllers()
  }

  private def bindControllers(): Unit = {
    bind(classOf[EnrolmentController]).to(classOf[EnrolmentControllerImpl]).asEagerSingleton()
    bind(classOf[UtilitiesController]).to(classOf[UtilitiesControllerImpl]).asEagerSingleton()
    bind(classOf[ValidationController]).to(classOf[ValidationControllerImpl]).asEagerSingleton()
  }

  private def bindServices(): Unit = {
    bind(classOf[EnrolmentService]).to(classOf[EnrolmentServiceImpl]).asEagerSingleton()
    //bind(classOf[MetricsService]).to(classOf[MetricsServiceImpl]).asEagerSingleton()
    bind(classOf[UtilitiesService]).to(classOf[UtilitiesServiceImpl]).asEagerSingleton()
    bind(classOf[ValidationService]).to(classOf[ValidationServiceImpl]).asEagerSingleton()
  }

  private def bindRepositories(): Unit = {
    bind(classOf[OrgAccountRepository]).to(classOf[OrgAccountRepositoryImpl]).asEagerSingleton()
    bind(classOf[RegistrationCodeRepository]).to(classOf[RegistrationCodeRepositoryImpl]).asEagerSingleton()
    bind(classOf[UserAccountRepository]).to(classOf[UserAccountRepositoryImpl]).asEagerSingleton()
  }

  def bindOther(): Unit = {
    bind(classOf[ConfigurationLoader]).to(classOf[ConfigurationLoaderImpl]).asEagerSingleton()
  }
}
