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

import cats.data.EitherT
import cats.implicits.catsStdInstancesForFuture
import com.google.inject.Inject
import controllers.actions.{CheckForSubmissionAction, StandardActionSets}
import models.ReporterType.{Individual, Sole}
import models.UserAnswers
import models.error.ApiError
import models.error.ApiError.{AlreadyRegisteredError, MandatoryInformationMissingError, ServiceUnavailableError}
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, SafeId}
import models.requests.DataRequest
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc._
import services.{BusinessMatchingWithoutIdService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CheckYourAnswersValidator, CountryListFactory, UserAnswersHelper}
import viewmodels.checkAnswers.CheckYourAnswersViewModel
import views.html.{CheckYourAnswersView, ThereIsAProblemView}

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  subscriptionService: SubscriptionService,
  val controllerComponents: MessagesControllerComponents,
  countryFactory: CountryListFactory,
  controllerHelper: ControllerHelper,
  checkForSubmission: CheckForSubmissionAction,
  registrationService: BusinessMatchingWithoutIdService,
  view: CheckYourAnswersView,
  errorView: ThereIsAProblemView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with UserAnswersHelper {

  def onPageLoad(): Action[AnyContent] = (standardActionSets.identifiedUserWithData() andThen checkForSubmission) {
    implicit request =>
      getMissingAnswers(request.userAnswers) match {
        case Nil => Ok(view(CheckYourAnswersViewModel.buildPages(request.userAnswers, countryFactory)))
        case _   => Redirect(routes.InformationMissingController.onPageLoad())
      }
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val result = for {
        safeId         <- EitherT(getSafeIdFromRegistration())
        subscriptionID <- EitherT(subscriptionService.checkAndCreateSubscription(safeId, request.userAnswers, request.affinityGroup))
        result         <- EitherT.right[ApiError](controllerHelper.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionID))
      } yield result

      handleErrorResult(result, request.userAnswers)
  }

  private def handleErrorResult(result: EitherT[Future, ApiError, Result], userAnswers: UserAnswers)(implicit
    messages: Messages,
    rh: RequestHeader
  ): Future[Result] = {
    val isNotOrg: Boolean = userAnswers.get(ReporterTypePage) match {
      case Some(rt) => Seq(Sole, Individual).contains(rt)
      case None     => false
    }

    result.valueOrF {
      case MandatoryInformationMissingError(_) =>
        logger.warn("CheckYourAnswersController: Mandatory information is missing")
        Future.successful(Redirect(routes.InformationMissingController.onPageLoad()))
      case ServiceUnavailableError =>
        Future.successful(ServiceUnavailable(errorView()(rh, messages)))
      case AlreadyRegisteredError =>
        logger.warn("CheckYourAnswersController: Already Registered")
        redirectToAlreadyRegistered(isNotOrg)
      case _ =>
        Future.successful(InternalServerError(errorView()(rh, messages)))
    }
  }

  private def redirectToAlreadyRegistered(isNotOrg: Boolean) =
    if (isNotOrg) {
      Future.successful(Redirect(controllers.individual.routes.IndividualAlreadyRegisteredController.onPageLoad()))
    } else {
      Future.successful(Redirect(controllers.routes.PreRegisteredController.onPageLoad()))
    }

  private def getSafeIdFromRegistration()(implicit request: DataRequest[AnyContent], hc: HeaderCarrier): Future[Either[ApiError, SafeId]] =
    request.userAnswers.get(RegistrationInfoPage) match {
      case Some(registration) =>
        val safeId = registration match {
          case OrgRegistrationInfo(safeId, _, _) => safeId
          case IndRegistrationInfo(safeId)       => safeId
        }
        Future.successful(Right(safeId))
      case _ =>
        registrationService.registerWithoutId()
    }

  private def getMissingAnswers(userAnswers: UserAnswers): Seq[Page] = CheckYourAnswersValidator(userAnswers).validate

}
