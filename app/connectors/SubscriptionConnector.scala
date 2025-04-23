/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import cats.data.EitherT
import config.FrontendAppConfig
import models.SubscriptionID
import models.error.ApiError
import models.error.ApiError.{AlreadyRegisteredError, UnableToCreateEMTPSubscriptionError}
import models.subscription.request.{CreateSubscriptionRequest, ReadSubscriptionRequest, UpdateSubscriptionRequest}
import models.subscription.response.{CreateSubscriptionResponse, DisplaySubscriptionResponse}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class SubscriptionConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2) extends Logging {

  def readSubscription(
    readSubscriptionRequest: ReadSubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[DisplaySubscriptionResponse]] = {

    val submissionUrl = url"${config.businessMatchingUrl}/subscription/read-subscription"

    http
      .post(submissionUrl)
      .withBody(Json.toJson(readSubscriptionRequest))
      .execute[HttpResponse]
      .map {
        case responseMessage if is2xx(responseMessage.status) =>
          responseMessage.json
            .asOpt[DisplaySubscriptionResponse]
        case errorStatus =>
          logger.warn(s"Status $errorStatus has been thrown when display subscription was called")
          None
      }
      .recover {
        case e: Exception =>
          logger.warn(s"S${e.getMessage} has been thrown when display subscription was called")
          None
      }
  }

  def updateSubscription(requestDetail: UpdateSubscriptionRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val updateSubscriptionUrl = url"${config.businessMatchingUrl}/subscription/update-subscription"
    http
      .put(updateSubscriptionUrl)
      .withBody(Json.toJson(requestDetail))
      .execute[HttpResponse]
      .map {
        responseMessage =>
          logger.info(s"updateSubscription: Status ${responseMessage.status} has been received when update subscription was called")
          is2xx(responseMessage.status)
      }
  }

  def createSubscription(
    createSubscriptionRequest: CreateSubscriptionRequest
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, SubscriptionID] = {

    val submissionUrl = url"${config.businessMatchingUrl}/subscription/create-subscription"

    EitherT {
      http
        .post(submissionUrl)
        .withBody(Json.toJson(createSubscriptionRequest))
        .execute[HttpResponse]
        .map {
          response =>
            if (is2xx(response.status)) {
              response.json.asOpt[CreateSubscriptionResponse] match {
                case Some(successResponse) => Right(successResponse.subscriptionId)
                case None                  => Left(UnableToCreateEMTPSubscriptionError)
              }
            } else {
              handleErrorResponse(response)
            }
        }
    }
  }

  private def handleErrorResponse(response: HttpResponse): Either[ApiError, SubscriptionID] = {
    val jsonBody = Try(Json.parse(response.body)).toOption.getOrElse(Json.obj())
    (jsonBody \ "status").asOpt[String] match {
      case Some("already_registered") =>
        logger.warn("Subscription already exists.")
        Left(AlreadyRegisteredError)

      case _ =>
        logger.warn(s"Received error response from backend: ${response.status}")
        Left(UnableToCreateEMTPSubscriptionError)
    }
  }

}
