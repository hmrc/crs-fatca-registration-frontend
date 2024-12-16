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
import models.enrolment.SubscriptionInfo
import models.error.ApiError
import models.error.ApiError.{ServiceUnavailableError, UnableToCreateEnrolmentError}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpErrorFunctions.{is2xx, is4xx}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxEnrolmentsConnector @Inject() (
  val config: FrontendAppConfig,
  val http: HttpClientV2
) extends Logging {

  def createEnrolment(
    enrolmentInfo: SubscriptionInfo
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): EitherT[Future, ApiError, Int] = {

    val url = url"${config.taxEnrolmentsUrl1}/${config.enrolmentKey}/${config.taxEnrolmentsUrl2}"

    val enrolmentRequest = Json.toJson(enrolmentInfo.convertToEnrolmentRequest)

    EitherT {
      http.put(url)
        .withBody(Json.toJson(enrolmentRequest))
        .execute[HttpResponse]
        .map {
          case responseMessage if is2xx(responseMessage.status) =>
            Right(responseMessage.status)
          case responseMessage if is4xx(responseMessage.status) =>
            logger.error(s"Error with tax-enrolments call  ${responseMessage.status} : ${responseMessage.body}")
            Left(UnableToCreateEnrolmentError)
          case responseMessage =>
            logger.error(s"Service error when creating enrolment  ${responseMessage.status} : ${responseMessage.body}")
            Left(ServiceUnavailableError)
        }
    }
  }

}
