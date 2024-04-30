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

package controllers.changeContactDetails

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import models.requests.DataRequestWithUserAnswers
import models.subscription.response.DisplaySubscriptionResponse
import pages.changeContactDetails.ChangeContactDetailsInProgressPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.{ChangeIndividualContactDetailsHelper, CountryListFactory}
import views.html.ThereIsAProblemView
import viewmodels.govuk.summarylist._

import views.html.changeContactDetails.ChangeIndividualContactDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ChangeIndividualContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  frontendAppConfig: FrontendAppConfig,
  standardActionSets: StandardActionSets,
  countryFactory: CountryListFactory,
  subscriptionService: SubscriptionService,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: ChangeIndividualContactDetailsView,
  errorView: ThereIsAProblemView
)(implicit executionContext: ExecutionContext) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = standardActionSets.subscriptionIdWithChangeDetailsRetrievalForIndividual().async {
    implicit request =>
      subscriptionService.getSubscription(request.subscriptionId).flatMap {
        case Some(subscriptionResponse) =>
          if (request.userAnswers.get(ChangeContactDetailsInProgressPage).isEmpty) {
            subscriptionService.populateUserAnswersFromSubscription(request.userAnswers, subscriptionResponse.success) match {
              case Some(userAnswers) =>
                val response = for {
                  updatedUserAnswers <- Future.fromTry(userAnswers.set(ChangeContactDetailsInProgressPage, true))
                  _                  <- sessionRepository.set(updatedUserAnswers)
                } yield createResponsePage(updatedUserAnswers, subscriptionResponse)
                response.flatten
              case _ =>
                logger.warn(s"Failed to populate user answers from subscription response for ${request.subscriptionId.value}")
                Future.successful(InternalServerError(errorView()))
            }
          } else {
            createResponsePage(request.userAnswers, subscriptionResponse)
          }
        case None =>
          logger.warn(s"Failed to retrieve subscription for ${request.subscriptionId.value}")
          Future.successful(InternalServerError(errorView()))
      }
  }

  def onSubmit: Action[AnyContent] = standardActionSets.subscriptionIdWithChangeDetailsRetrievalForIndividual().async {
    implicit request =>
      subscriptionService.updateContactDetails(request.subscriptionId, request.userAnswers) map {
        case true =>
          request.userAnswers.remove(ChangeContactDetailsInProgressPage) match {
            case Success(_) =>
              Redirect(controllers.routes.DetailsUpdatedController.onPageLoad())
            case Failure(exception) =>
              logger.warn(s"Failed to remove $ChangeContactDetailsInProgressPage", exception)
              InternalServerError(errorView())
          }
        case false => InternalServerError(errorView())
      }
  }

  private def createResponsePage(
    userAnswers: UserAnswers,
    subscriptionResponse: DisplaySubscriptionResponse
  )(implicit request: DataRequestWithUserAnswers[AnyContent]): Future[Result] = {
    val helper      = new ChangeIndividualContactDetailsHelper(userAnswers)
    val contactList = SummaryListViewModel(helper.changeIndividualContactDetails)

    subscriptionService.checkIfContactDetailsHasChanged(subscriptionResponse, userAnswers) match {
      case Some(hasChanges) => Future.successful(Ok(view(contactList, frontendAppConfig, hasChanges)))
      case _                => Future.successful(InternalServerError(errorView()))
    }
  }

}
