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

package controllers

import models.error.ApiError.ServiceUnavailableError
import models.matching.SafeId
import models.requests.DataRequest
import models.{SubscriptionID, UserAnswers}
import pages.SubscriptionIDPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TaxEnrolmentService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThereIsAProblemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllerHelper @Inject() (
  val controllerComponents: MessagesControllerComponents,
  taxEnrolmentService: TaxEnrolmentService,
  errorView: ThereIsAProblemView,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def createEnrolment(safeId: SafeId, userAnswers: UserAnswers, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    taxEnrolmentService.checkAndCreateEnrolment(safeId, userAnswers, subscriptionId) flatMap {
      case Right(_) =>
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())) // TODO DAC6-2756 and DAC6-2858
      case Left(error) =>
        logger.warn(s"Error received from API: $error")
        error match {
          case ServiceUnavailableError =>
            Future.successful(ServiceUnavailable(errorView()))
          case _ =>
            Future.successful(InternalServerError(errorView()))
        }
    }

  def updateSubscriptionIdAndCreateEnrolment(safeId: SafeId, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionIDPage, subscriptionId))
      _              <- sessionRepository.set(updatedAnswers)
      result         <- createEnrolment(safeId, request.userAnswers, subscriptionId)
    } yield result

}
