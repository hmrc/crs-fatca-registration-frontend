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

import controllers.actions._
import forms.ReporterTypeFormProvider
import models.Mode
import navigation.Navigator
import pages.ReporterTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ReporterTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReporterTypeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: ReporterTypeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ReporterTypeView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithInitializedData() {
    implicit request =>
      val preparedForm = request.userAnswers.get(ReporterTypePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedUserWithInitializedData().async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val current = request.userAnswers.get(ReporterTypePage).filter(_ == value)
            for {
              updatedAnswers <- current.fold(Future.fromTry(request.userAnswers.set(ReporterTypePage, value)))(
                _ => Future.successful(request.userAnswers)
              )
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ReporterTypePage, mode, updatedAnswers))
          }
        )
  }

}
