/*
 *  Copyright 2018 CJWW Development
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package helpers.other

import akka.util.Timeout
import org.scalatest.Assertion
import play.api.mvc.Result
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.Future
import scala.concurrent.duration._

trait FutureAsserts
  extends FutureAwaits
    with DefaultAwaitTimeout {

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  def awaitAndAssert[T](methodUnderTest: => Future[T])(assertions: T => Assertion): Assertion = {
    assertions(await(methodUnderTest))
  }

  def assertResult[T](methodUnderTest: => Future[Result])(assertions: Future[Result] => Assertion): Assertion = {
    assertions(methodUnderTest)
  }
}
