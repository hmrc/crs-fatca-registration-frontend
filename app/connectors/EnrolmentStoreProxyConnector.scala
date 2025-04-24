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
import models.enrolment.{EnrolmentResponse, GroupIds}
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, MalformedError}
import play.api.Logging
import play.api.http.Status.NO_CONTENT
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpErrorFunctions.is2xx
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EnrolmentStoreProxyConnector @Inject() (val config: FrontendAppConfig, val http: HttpClientV2) extends Logging {

  def enrolmentStatus(subscriptionID: SubscriptionID)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): EitherT[Future, ApiError, Unit] = {
    val serviceEnrolmentPattern                             = s"HMRC-FATCA-ORG~FATCAID~${subscriptionID.value}"
    val espUrl                                              = url"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments/$serviceEnrolmentPattern/groups"
    val esResponse: EitherT[Future, ApiError, HttpResponse] = EitherT.right(http.get(espUrl).execute[HttpResponse])
    EitherT {
      esResponse.value.flatMap {
        case Right(response) => response.status match {
            case NO_CONTENT    => Future.successful(Right(()))
            case s if is2xx(s) => parseAndAssertNoExisting(response.json).value
            case other =>
              logger.error(s"Enrolment Store Proxy error: ${response.status} - ${response.body}")
              Future.successful(Left(MalformedError(other)))
          }
        case Left(error) =>
          logger.error(s"Enrolment Store Proxy error: $error")
          Future.successful(Left(error))
      }
    }
  }

  private def parseAndAssertNoExisting(json: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, Unit] =
    json.asOpt[GroupIds] match {
      case None                                                 => EitherT.rightT(())
      case Some(groupIds) if groupIds.principalGroupIds.isEmpty => EitherT.rightT(())
      case Some(groupIds) => EitherT {
          checkGroupEnrolments(groupIds.principalGroupIds).map {
            case true =>
              logger.warn(s"Enrolment already exists for groupIds: ${groupIds.principalGroupIds}")
              Left(EnrolmentExistsError(groupIds))
            case false => Right(())
          }
        }
    }

  private def checkGroupEnrolments(groupIds: Seq[String])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val groups = groupIds.toSet
    val calls: Set[Future[Boolean]] = groups.map {
      groupId =>
        val url = url"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments/$groupId/groups"
        http
          .get(url)
          .execute[HttpResponse]
          .map {
            case response if is2xx(response.status) =>
              response.json
                .asOpt[EnrolmentResponse].exists(_.enrolments.nonEmpty)
            case response =>
              logger.warn(s"Enrolment response not formed. ${response.status} response status")
              false
          }
    }
    Future.foldLeft(calls)(false)(_ || _)
  }

}
