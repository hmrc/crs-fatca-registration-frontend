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
import models.error.ApiError
import models.error.ApiError.{BadRequestError, NotFoundError, ServiceUnavailableError}
import models.matching.SafeId
import models.register.request.{RegisterWithID, RegisterWithoutID}
import models.register.response.{RegistrationWithIDResponse, RegistrationWithoutIDResponse}
import play.api.Logging
import play.api.http.Status._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReads.is2xx
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject() (val config: FrontendAppConfig, val http: HttpClient) extends Logging {

  val submissionUrl = s"${config.businessMatchingUrl}/registration"

  def withIndividualNino(
    registration: RegisterWithID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, RegistrationWithIDResponse] =
    registerWithID(registration, "/individual/nino")

  def withOrganisationUtr(
    registration: RegisterWithID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, RegistrationWithIDResponse] =
    registerWithID(registration, "/organisation/utr")

  private def registerWithID(
    registration: RegisterWithID,
    endpoint: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, RegistrationWithIDResponse] =
    EitherT {
      http.POST[RegisterWithID, HttpResponse](s"$submissionUrl$endpoint", registration) map {
        case responseMessage if is2xx(responseMessage.status) =>
          Right(responseMessage.json.as[RegistrationWithIDResponse])
        case responseMessage => handleError(responseMessage, endpoint)
      }
    }

  def withIndividualNoId(
    registration: RegisterWithoutID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, SafeId]] =
    registerWithoutID(registration, "/individual/noId")

  def withOrganisationNoId(
    registration: RegisterWithoutID
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, SafeId]] =
    registerWithoutID(registration, "/organisation/noId")

  private def registerWithoutID(
    registration: RegisterWithoutID,
    endpoint: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ApiError, SafeId]] =
    http.POST[RegisterWithoutID, HttpResponse](s"$submissionUrl$endpoint", registration) map {
      case responseMessage if is2xx(responseMessage.status) =>
        responseMessage.json.as[RegistrationWithoutIDResponse].registerWithoutIDResponse.safeId match {
          case Some(safeId) => Right(safeId)
          case _ =>
            logger.warn(s"Error in registration with $endpoint: safeId is missing.")
            Left(NotFoundError)
        }
      case responseMessage => handleError(responseMessage, endpoint)
    }

  def handleError[A](responseMessage: HttpResponse, endpoint: String): Either[ApiError, A] =
    responseMessage.status match {
      case NOT_FOUND =>
        logger.warn(s"Error in registration with $endpoint: not found.")
        Left(NotFoundError)
      case BAD_REQUEST =>
        logger.warn(s"Error in registration with $endpoint: invalid.")
        Left(BadRequestError)
      case responseMessage =>
        logger.warn(s"Error in registration with $endpoint: $responseMessage.")
        Left(ServiceUnavailableError)
    }

}
