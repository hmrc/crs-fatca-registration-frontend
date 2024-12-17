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
import models.error.ApiError.{BadRequestError, DuplicateSubmissionError, NotFoundError, ServiceUnavailableError, UnableToCreateEMTPSubscriptionError}
import models.subscription.request.{CreateSubscriptionRequest, ReadSubscriptionRequest, UpdateSubscriptionRequest}
import models.subscription.response.{CreateSubscriptionResponse, DisplaySubscriptionResponse}
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, CONFLICT, NOT_FOUND, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
          case response if is2xx(response.status) =>
            response.json.asOpt[CreateSubscriptionResponse] match {
              case Some(response) => Right(response.subscriptionId)
              case _              => Left(UnableToCreateEMTPSubscriptionError)
            }
          case response if response.status equals CONFLICT =>
            logger.warn(s"Duplicate submission to ETMP. ${response.status} response status")
            Left(DuplicateSubmissionError)
          case response =>
            logger.warn(s"Unable to create a subscription to ETMP. ${response.status} response status")
            handleError(response)
        }
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

  private def handleError[A](responseMessage: HttpResponse): Either[ApiError, A] =
    responseMessage.status match {
      case NOT_FOUND =>
        Left(NotFoundError)
      case BAD_REQUEST =>
        Left(BadRequestError)
      case SERVICE_UNAVAILABLE =>
        Left(ServiceUnavailableError)
      case _ =>
        Left(UnableToCreateEMTPSubscriptionError)
    }

}
