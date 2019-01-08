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

package common

import com.cjwwdev.identifiers.IdentifierValidation
import com.cjwwdev.responses.ApiResponse
import com.cjwwdev.security.deobfuscation.DeObfuscator
import models.formatters.{APIFormatting, BaseFormatting}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait BackendController extends BaseController with IdentifierValidation with ApiResponse {
  implicit val formatter: BaseFormatting = APIFormatting

  implicit val ec: ExecutionContext

  def withEncryptedUrl[T](enc: String)(f: T => Future[Result])(implicit request: Request[_], deObfuscation: DeObfuscator[T]): Future[Result] = {
    deObfuscation.decrypt(enc).fold(
      data => f(data),
      err  => Try(Json.parse(err.message)).fold(
        _ => withFutureJsonResponseBody(BAD_REQUEST, s"Couldn't decrypt request body on ${request.path}") { json =>
          Future.successful(BadRequest(json))
        },
        jsError => withFutureJsonResponseBody(BAD_REQUEST, jsError, "Decrypted json was missing a field") { json =>
          Future.successful(BadRequest(json))
        }
      )
    )
  }
}
