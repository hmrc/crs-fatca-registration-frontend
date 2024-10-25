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

package controllers.organisation

import controllers.actions.{CheckForSubmissionAction, StandardActionSets}
import forms.BusinessNameFormProvider
import models.ReporterType._
import models.{Mode, ReporterType}
import navigation.Navigator
import pages.{BusinessNamePage, ReporterTypePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.BusinessNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: BusinessNameFormProvider,
  checkForSubmission: CheckForSubmissionAction,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def selectedReporterTypeText(reporterType: ReporterType): Option[String] =
    reporterType match {
      case LimitedPartnership | LimitedCompany => Some("llp")
      case Partnership                         => Some("partner")
      case UnincorporatedAssociation           => Some("unincorporated")
      case _                                   => None
    }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (standardActionSets.identifiedUserWithDependantAnswer(ReporterTypePage) andThen checkForSubmission) async {
      implicit request =>
        selectedReporterTypeText(request.userAnswers.get(ReporterTypePage).get) match {
          case Some(businessTypeText) =>
            val form = formProvider(businessTypeText)
            val preparedForm = request.userAnswers.get(BusinessNamePage) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Future.successful(Ok(view(preparedForm, mode, businessTypeText)))
          case _ => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    standardActionSets.identifiedUserWithDependantAnswer(ReporterTypePage).async {
      implicit request =>
        selectedReporterTypeText(request.userAnswers.get(ReporterTypePage).get) match {
          case Some(businessTypeText) =>
            formProvider(businessTypeText)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, businessTypeText))),
                value =>
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessNamePage, value))
                    _              <- sessionRepository.set(updatedAnswers)
                  } yield Redirect(navigator.nextPage(BusinessNamePage, mode, updatedAnswers))
              )
          case None => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
        }

    }

}
