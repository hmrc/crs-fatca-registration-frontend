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
import models.ReporterType.{LimitedCompany, UnincorporatedAssociation}

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{ReporterTypePage, WhatIsYourUTRPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.WhatIsYourUTRView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsYourUTRController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: WhatIsYourUTRFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatIsYourUTRView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      val taxType = getTaxType(request.userAnswers)
      val form    = formProvider(taxType)

      val preparedForm = request.userAnswers.get(WhatIsYourUTRPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, taxType))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithData().async {
    implicit request =>
      val taxType = getTaxType(request.userAnswers)
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

  private def getTaxType(userAnswers: UserAnswers)(implicit messages: Messages): String =
    userAnswers.get(ReporterTypePage) match {
      case Some(LimitedCompany) | Some(UnincorporatedAssociation) => messages("whatIsYourUTR.error.corporationTax")
      case _                                                      => messages("whatIsYourUTR.error.selfAssessment")
    }

}
