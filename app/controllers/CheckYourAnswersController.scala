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
import models.error.ApiError
import models.error.ApiError.{MandatoryInformationMissingError, ServiceUnavailableError}
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, SafeId}
import models.requests.DataRequest
import models.{ReporterType, UserAnswers}
import pages._
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{BusinessMatchingWithoutIdService, SubscriptionService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{CountryListFactory, UserAnswersHelper}
import viewmodels.Section
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
      val viewModel: Seq[Section] =
        CheckYourAnswersViewModel.buildPages(request.userAnswers, countryFactory)
      Ok(view(viewModel))
  }

  def onSubmit(): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val result = for {
        safeId         <- EitherT(getSafeIdFromRegistration())
        subscriptionID <- EitherT(subscriptionService.checkAndCreateSubscription(safeId, request.userAnswers, request.affinityGroup))
        result         <- EitherT.right[ApiError](controllerHelper.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionID))
      } yield result

      result.valueOrF {
        case MandatoryInformationMissingError(_) =>
          logger.warn("CheckYourAnswersController: Mandatory information is missing")
          Future.successful(Redirect(routes.InformationMissingController.onPageLoad()))
        case error =>
          logger.warn(s"Error received from API: $error")
          error match {
            case ServiceUnavailableError =>
              Future.successful(ServiceUnavailable(errorView()))
            case _ =>
              Future.successful(InternalServerError(errorView()))
          }
      }

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

  def getPagesMissingAnswers(userAnswers: UserAnswers): Seq[Page] = {
    val reporterType = userAnswers.get(ReporterTypePage)

    if (reporterType.contains(ReporterType.Individual)) {
      val businessDetails = userAnswers.get(IndDoYouHaveNINumberPage) match {
        case Some(false) => getIndividualWithoutIdMissingAnswers(userAnswers)
        case _           => Seq(IndDoYouHaveNINumberPage)
      }

      businessDetails ++ Seq(
        if (userAnswers.get(IndContactEmailPage).nonEmpty) None else Some(IndContactEmailPage),
        userAnswers.get(IndContactHavePhonePage) match {
          case None        => Some(IndContactHavePhonePage)
          case Some(false) => None
          case Some(true) =>
            if (userAnswers.get(IndContactPhonePage).nonEmpty) None else Some(IndContactPhonePage)
        }
      ).filter(_.nonEmpty).map(_.get)
    } else {
      Seq(ReporterTypePage)
    }
  }

  private def getIndividualWithoutIdMissingAnswers(userAnswers: UserAnswers): Seq[Page] =
    Seq(
      if (userAnswers.get(IndWhatIsYourNamePage).nonEmpty) None else Some(IndWhatIsYourNamePage)
    ).filter(_.nonEmpty).map(_.get)

}
