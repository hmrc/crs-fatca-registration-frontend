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
import models.enrolment.GroupIds
import models.error.ApiError
import models.error.ApiError.{EnrolmentExistsError, MalformedError}
import play.api.Logging
import play.api.http.Status.NO_CONTENT
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
    val serviceEnrolmentPattern = s"HMRC-FATCA-ORG~FATCAID~${subscriptionID.value}"
    val submissionUrl           = url"${config.enrolmentStoreProxyUrl}/enrolment-store/enrolments/$serviceEnrolmentPattern/groups"
    EitherT {
      http
        .get(submissionUrl)
        .execute[HttpResponse]
        .map {
          case response if response.status == NO_CONTENT => Right(())
          case response if is2xx(response.status) =>
            response.json
              .asOpt[GroupIds]
              .map(
                groupIds =>
                  if (groupIds.principalGroupIds.nonEmpty) {
                    Left(EnrolmentExistsError(groupIds))
                  } else {
                    Right(())
                  }
              )
              .getOrElse(Right(()))
          case response =>
            logger.warn(s"Enrolment response not formed. ${response.status} response status")
            Left(MalformedError(response.status))
        }
    }
  }

}
