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

import javax.inject.Inject

import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//class MetricsServiceImpl @Inject()(val metrics: Metrics) extends MetricsService
//
//trait MetricsService {
//  val metrics: Metrics
//
//  val mongoResponseTimer = metrics.defaultRegistry.timer("mongo-response-timer")
//
//  def runMetricsTimer[T](timer: Timer.Context)(f: => Future[T]): Future[T] = {
//    f map { data =>
//      timer.stop()
//      data
//    } recover {
//      case e =>
//        timer.stop()
//        throw e
//    }
//  }
//}
