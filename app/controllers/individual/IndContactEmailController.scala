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

package controllers.individual

import controllers.ControllerHelper
import controllers.actions._
import forms.IndContactEmailFormProvider
import models.Mode
import models.matching.{IndRegistrationInfo, OrgRegistrationInfo, SafeId}
import models.requests.DataRequest
import navigation.Navigator
import pages.{IndContactEmailPage, RegistrationInfoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.individual.IndContactEmailView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndContactEmailController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: IndContactEmailFormProvider,
  checkForSubmission: CheckForSubmissionAction,
  subscriptionService: SubscriptionService,
  controllerHelper: ControllerHelper,
  val controllerComponents: MessagesControllerComponents,
  view: IndContactEmailView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.identifiedUserWithData() andThen checkForSubmission) async {
    implicit request =>
      val preparedForm = request.userAnswers.get(IndContactEmailPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      getSafeIdFromRegistration() match {
        case Some(safeId) => subscriptionService.getSubscription(safeId).flatMap {
            case Some(subscriptionID) =>
              controllerHelper.updateSubscriptionIdAndCreateEnrolment(safeId, subscriptionID)
            case _ => Future.successful(Ok(view(preparedForm, mode)))
          }
        case _ =>
          Future.successful(Ok(view(preparedForm, mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(IndContactEmailPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(IndContactEmailPage, mode, updatedAnswers))
        )
  }

  private def getSafeIdFromRegistration()(implicit request: DataRequest[AnyContent]): Option[SafeId] =
    request.userAnswers.get(RegistrationInfoPage) match {
      case Some(registration) =>
        registration match {
          case OrgRegistrationInfo(safeId, _, _) => Some(safeId)
          case IndRegistrationInfo(safeId)       => Some(safeId)
        }
      case _ => None
    }

}
