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

import com.cjwwdev.config.{ConfigurationLoader, ConfigurationLoaderImpl}
import com.cjwwdev.mongo.indexes.RepositoryIndexer
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
    bind(classOf[ClassRoomController]).to(classOf[ClassRoomControllerImpl]).asEagerSingleton()
  }

  private def bindServices(): Unit = {
    bind(classOf[EnrolmentService]).to(classOf[EnrolmentServiceImpl]).asEagerSingleton()
    bind(classOf[UtilitiesService]).to(classOf[UtilitiesServiceImpl]).asEagerSingleton()
    bind(classOf[ValidationService]).to(classOf[ValidationServiceImpl]).asEagerSingleton()
    bind(classOf[ClassRoomService]).to(classOf[ClassRoomServiceImpl]).asEagerSingleton()
  }

  private def bindRepositories(): Unit = {
    bind(classOf[OrgAccountRepository]).to(classOf[OrgAccountRepositoryImpl]).asEagerSingleton()
    bind(classOf[RegistrationCodeRepository]).to(classOf[RegistrationCodeRepositoryImpl]).asEagerSingleton()
    bind(classOf[UserAccountRepository]).to(classOf[UserAccountRepositoryImpl]).asEagerSingleton()
    bind(classOf[ClassRoomRepository]).to(classOf[ClassRoomRepositoryImpl]).asEagerSingleton()
    bind(classOf[RepositoryIndexer]).to(classOf[DeversityIndexing]).asEagerSingleton()
  }

  def bindOther(): Unit = {
    bind(classOf[ConfigurationLoader]).to(classOf[ConfigurationLoaderImpl]).asEagerSingleton()
  }
}
