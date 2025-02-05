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

import models.error.ApiError.{EnrolmentExistsError, MandatoryInformationMissingError, ServiceUnavailableError}
import models.matching.SafeId
import models.requests.DataRequest
import models.{ReporterType, SubscriptionID, UserAnswers, UserSubscription}
import pages.{ReporterTypePage, SubscriptionIDPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import repositories.{SessionRepository, SubscriptionRepository}
import services.TaxEnrolmentService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThereIsAProblemView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllerHelper @Inject() (
  val controllerComponents: MessagesControllerComponents,
  taxEnrolmentService: TaxEnrolmentService,
  errorView: ThereIsAProblemView,
  sessionRepository: SessionRepository,
  subscriptionRepository: SubscriptionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def indAlreadyRegistered(implicit request: DataRequest[AnyContent]) = {

    val isNonOrgReporter: Boolean = request.userAnswers.get(ReporterTypePage).exists(ReporterType.nonOrgReporterTypes.contains)

    isNonOrgReporter || request.affinityGroup == AffinityGroup.Individual
  }

  private def createEnrolment(safeId: SafeId, userAnswers: UserAnswers, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    taxEnrolmentService.checkAndCreateEnrolment(userAnswers, subscriptionId) flatMap {
      case Right(_) =>
        subscriptionRepository.clear(safeId.value).map(
          _ => Redirect(routes.RegistrationConfirmationController.onPageLoad())
        )
      case Left(EnrolmentExistsError(groupIds)) if indAlreadyRegistered =>
        logger.info(s"ControllerHelper: EnrolmentExistsError for the the groupIds $groupIds")
        subscriptionRepository.set(UserSubscription(safeId.value, subscriptionId))
          .map(
            _ => Redirect(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad())
          )
      case Left(EnrolmentExistsError(groupIds)) =>
        logger.info(s"ControllerHelper: EnrolmentExistsError for the the groupIds $groupIds")
        subscriptionRepository.set(UserSubscription(safeId.value, subscriptionId))
          .map(
            _ => Redirect(routes.PreRegisteredController.onPageLoad())
          )
      case Left(MandatoryInformationMissingError(_)) =>
        logger.warn(s"ControllerHelper: Mandatory information is missing")
        subscriptionRepository.set(UserSubscription(safeId.value, subscriptionId))
          .map(
            _ => Redirect(routes.InformationMissingController.onPageLoad())
          )

      case Left(error) =>
        logger.warn(s"Error received from API: $error")
        error match {
          case ServiceUnavailableError =>
            subscriptionRepository.set(UserSubscription(safeId.value, subscriptionId))
              .map(
                _ => ServiceUnavailable(errorView())
              )
          case _ =>
            subscriptionRepository.set(UserSubscription(safeId.value, subscriptionId))
              .map(
                _ => InternalServerError(errorView())
              )
        }
    }

  def updateSubscriptionIdAndCreateEnrolment(safeId: SafeId, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    for {
      result         <- createEnrolment(safeId, request.userAnswers, subscriptionId)
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionIDPage, subscriptionId))
      _              <- sessionRepository.set(updatedAnswers)
    } yield result

}
