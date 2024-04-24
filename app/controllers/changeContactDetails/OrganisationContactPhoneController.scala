/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions._
import forms.changeContactDetails.OrganisationPhoneFormProvider
import models.Mode
import navigation.Navigator
import pages.changeContactDetails.OrganisationContactPhonePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.ContactHelper
import views.html.changeContactDetails.OrganisationContactPhoneView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OrganisationContactPhoneController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  formProvider: OrganisationPhoneFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: OrganisationContactPhoneView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ContactHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = standardActionSets.subscriptionIdWithChangeDetailsRequiredForOrgOrAgent() {
    implicit request =>
      val preparedForm = request.userAnswers.get(OrganisationContactPhonePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, getOrganisationContactName(request.userAnswers)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.subscriptionIdWithChangeDetailsRequiredForOrgOrAgent().async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, getOrganisationContactName(request.userAnswers)))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(OrganisationContactPhonePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(OrganisationContactPhonePage, mode, updatedAnswers))
        )
  }

}
