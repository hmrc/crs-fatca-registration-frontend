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

package services

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import connectors.SubscriptionConnector
import models.error.ApiError
import models.error.ApiError.MandatoryInformationMissingError
import models.matching.SafeId
import models.subscription.request.{CreateSubscriptionRequest, DisplaySubscriptionRequest, SubscriptionRequest}
import models.{SubscriptionID, UserAnswers}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubscriptionService @Inject() (val subscriptionConnector: SubscriptionConnector) {

  def checkAndCreateSubscription(safeID: SafeId, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Either[ApiError, SubscriptionID]] =
    getDisplaySubscriptionId(safeID) flatMap {
      case Some(subscriptionID) =>
        EitherT.rightT(subscriptionID).value
      case _ =>
        (SubscriptionRequest.convertTo(safeID, userAnswers) match {
          case Some(subscriptionRequest) =>
            val response = subscriptionConnector.createSubscription(CreateSubscriptionRequest(subscriptionRequest))

            response
          case _ =>
            EitherT.leftT(MandatoryInformationMissingError())
        }).value
    }

  def getDisplaySubscriptionId(safeId: SafeId)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[Option[SubscriptionID]] = {
    val displaySubscription: DisplaySubscriptionRequest = DisplaySubscriptionRequest.convertTo(safeId.value)
    subscriptionConnector.readSubscription(displaySubscription)
  }

}
