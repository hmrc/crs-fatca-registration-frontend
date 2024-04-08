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

package controllers.actions

import models.UserAnswers
import models.requests.{DataRequestWithSubscriptionId, DataRequestWithUserAnswers}
import play.api.mvc.ActionTransformer
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChangeDetailsDataRetrievalActionImpl @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends ChangeDetailsDataRetrievalAction {

  override def apply(): ActionTransformer[DataRequestWithSubscriptionId, DataRequestWithUserAnswers] =
    new ChangeDetailsDataRetrievalActionProvider(sessionRepository)

}

class ChangeDetailsDataRetrievalActionProvider @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[DataRequestWithSubscriptionId, DataRequestWithUserAnswers] {

  override protected def transform[A](request: DataRequestWithSubscriptionId[A]): Future[DataRequestWithUserAnswers[A]] =
    sessionRepository.get(request.userId).map {
      maybeUserAnswers =>
        val userAnswers = maybeUserAnswers.getOrElse(UserAnswers(request.userId))
        DataRequestWithUserAnswers(request.request, request.userId, request.subscriptionId, userAnswers)
    }

}

trait ChangeDetailsDataRetrievalAction {
  def apply(): ActionTransformer[DataRequestWithSubscriptionId, DataRequestWithUserAnswers]
}
