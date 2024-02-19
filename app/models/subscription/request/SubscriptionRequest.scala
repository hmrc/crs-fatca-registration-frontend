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

package models.subscription.request

import models.UserAnswers
import models.matching.SafeId
import play.api.libs.json.{Json, OFormat}

case class SubscriptionRequest(requestDetail: CreateSubscriptionDetailsRequest)

object SubscriptionRequest {
  implicit val format: OFormat[SubscriptionRequest] = Json.format[SubscriptionRequest]

  def convertTo(safeID: SafeId, userAnswers: UserAnswers): Option[SubscriptionRequest] =
    CreateSubscriptionDetailsRequest.convertTo(safeID, userAnswers) map {
      requestDetails =>
        SubscriptionRequest(requestDetails)
    }

}
