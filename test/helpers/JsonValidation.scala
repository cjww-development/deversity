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
package helpers

import org.scalatestplus.play.PlaySpec
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsPath, JsResult, JsSuccess}

trait JsonValidation {

  this: PlaySpec =>

  def shouldHaveErrors[T](result: JsResult[T], expectedErrors: Map[JsPath, Seq[ValidationError]]): Unit = {
    result match {
      case JsSuccess(data, _) => fail(s"read should have failed and didn't - produced $data")
      case JsError(errors) =>
        for(error <- errors) {
          error match {
            case (path, valErrs) =>
              expectedErrors.keySet must contain(path)
              expectedErrors(path) mustBe valErrs
          }
        }
    }
  }
}
