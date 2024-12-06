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
import models.requests.DataRequest
import models.{ReporterType, SubscriptionID, UserAnswers}
import pages.{RegistrationInfoPage, ReporterTypePage, SubscriptionIDPage}
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
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
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private def indAlreadyRegistered(implicit request: DataRequest[AnyContent]) =
    request.affinityGroup == AffinityGroup.Individual ||
      (request.affinityGroup == AffinityGroup.Agent && request.userAnswers.get(ReporterTypePage).exists(ReporterType.nonOrgReporterTypes.contains))

  private def createEnrolment(userAnswers: UserAnswers, subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    taxEnrolmentService.checkAndCreateEnrolment(userAnswers, subscriptionId) flatMap {
      case Right(_) =>
        Future.successful(Redirect(routes.RegistrationConfirmationController.onPageLoad()))
      case Left(EnrolmentExistsError(groupIds)) if indAlreadyRegistered =>
        logger.info(s"ControllerHelper: EnrolmentExistsError for the the groupIds $groupIds")
        Future.successful(Redirect(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad()))
      case Left(EnrolmentExistsError(groupIds)) =>
        logger.info(s"ControllerHelper: EnrolmentExistsError for the the groupIds $groupIds")
        if (request.userAnswers.get(RegistrationInfoPage).isDefined) {
          Future.successful(Redirect(routes.PreRegisteredController.onPageLoad(true)))
        } else {
          Future.successful(Redirect(routes.PreRegisteredController.onPageLoad(false)))
        }
      case Left(MandatoryInformationMissingError(_)) =>
        logger.warn(s"ControllerHelper: Mandatory information is missing")
        Future.successful(Redirect(routes.InformationMissingController.onPageLoad()))

      case Left(error) =>
        logger.warn(s"Error received from API: $error")
        error match {
          case ServiceUnavailableError =>
            Future.successful(ServiceUnavailable(errorView()))
          case _ =>
            Future.successful(InternalServerError(errorView()))
        }
    }

  def updateSubscriptionIdAndCreateEnrolment(subscriptionId: SubscriptionID)(implicit
    hc: HeaderCarrier,
    request: DataRequest[AnyContent]
  ): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(SubscriptionIDPage, subscriptionId))
      _              <- sessionRepository.set(updatedAnswers)
      result         <- createEnrolment(request.userAnswers, subscriptionId)
    } yield result

}
