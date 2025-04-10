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

import controllers.actions._
import forms.WhatIsYourUTRFormProvider
import models.ReporterType.{LimitedCompany, LimitedPartnership, Partnership, Sole, UnincorporatedAssociation}
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{ReporterTypePage, WhatIsYourUTRPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.WhatIsYourUTRView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourUTRController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: WhatIsYourUTRFormProvider,
  checkForSubmission: CheckForSubmissionAction,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsYourUTRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.identifiedUserWithData() andThen checkForSubmission) async {
    implicit request =>
      request.request.
      val taxType = getTaxTypeMessageKey(request.userAnswers)
      val form    = formProvider(taxType)

      val preparedForm = request.userAnswers.get(WhatIsYourUTRPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Future.successful(Ok(view(preparedForm, mode, taxType)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val taxType = getTaxTypeMessageKey(request.userAnswers)
      val form    = formProvider(taxType)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, taxType))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsYourUTRPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatIsYourUTRPage, mode, updatedAnswers))
        )
  }

  private def getTaxTypeMessageKey(userAnswers: UserAnswers): String =
    userAnswers.get(ReporterTypePage) match {
      case Some(LimitedCompany) | Some(UnincorporatedAssociation) => "whatIsYourUTR.corporation"
      case Some(Partnership) | Some(LimitedPartnership)           => "whatIsYourUTR.partnership"
      case Some(Sole)                                             => "whatIsYourUTR.soleTrader"
    }

}
