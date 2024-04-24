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

package models.requests

import models.{SubscriptionID, UserAnswers}
import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.AffinityGroup

case class OptionalDataRequest[A](request: Request[A], userId: String, affinityGroup: AffinityGroup, userAnswers: Option[UserAnswers])
    extends WrappedRequest[A](request)

case class DataRequest[A](request: Request[A], userId: String, affinityGroup: AffinityGroup, userAnswers: UserAnswers) extends WrappedRequest[A](request)

case class DataRequestWithUserAnswers[A](
  request: Request[A],
  userId: String,
  subscriptionId: SubscriptionID,
  userAnswers: UserAnswers
) extends WrappedRequest[A](request)

case class DataRequestWithSubscriptionId[A](
  request: Request[A],
  userId: String,
  subscriptionId: SubscriptionID
) extends WrappedRequest[A](request)
