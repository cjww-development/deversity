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

import com.cjwwdev.request.RequestBuilder
import com.cjwwdev.request.RequestBuilder.stringRequestBuilder
import com.cjwwdev.responses.ApiResponse
import javax.inject.Inject
import play.api.http.HttpErrorHandler
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.mvc.Results.Status
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

class ErrorHandler @Inject()() extends HttpErrorHandler with ApiResponse {
  private val notFoundErrorBody: String = "No matching resource found for this request"
  private val notFoundErrorMessage: String => String = reqId => s"Logged against requestId $reqId"

  private val internalServerErrorBody: String = "There was a problem when processing your request"
  private val internalServerErrorMessage: String => String = reqId => s"Exception has been logged against requestId $reqId"

  private val requestId: RequestHeader => String = _.headers.get("requestId").getOrElse("-")

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    implicit val req = RequestBuilder.buildEmptyRequest(request)
    statusCode match {
      case status@NOT_FOUND => withFutureJsonResponseBody(status, notFoundErrorBody, notFoundErrorMessage(requestId(request))) { json =>
        Future.successful(Status(status)(json))
      }
      case status => withFutureJsonResponseBody(status, "There was a client side problem preventing your request to be processed, check your request") { json =>
        Future.successful(Status(status)(json))
      }
    }
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    implicit val req = RequestBuilder.buildEmptyRequest(request)
    withFutureJsonResponseBody(INTERNAL_SERVER_ERROR, exception.getMessage, internalServerErrorMessage(requestId(request))) { json =>
      Future.successful(Status(INTERNAL_SERVER_ERROR)(json))
    }
  }
}
