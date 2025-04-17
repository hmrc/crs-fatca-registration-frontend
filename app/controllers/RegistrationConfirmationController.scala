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

package controllers

import controllers.actions._
import pages.SubscriptionIDPage

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{PageUnavailableView, RegistrationConfirmationView, ThereIsAProblemView}

import scala.concurrent.{ExecutionContext, Future}

class RegistrationConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  view: RegistrationConfirmationView,
  errorView: PageUnavailableView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = standardActionSets.identifiedWithoutEnrolmentCheckAndWithoutRedirect() async {
    implicit request =>
      for {
        subscriptionId <- Future.successful(request.userAnswers.get(SubscriptionIDPage))
        clearSession   <- sessionRepository.set(request.userAnswers.copy(data = Json.obj()))
      } yield (subscriptionId, clearSession) match {
        case (Some(fatcaId), true) => Ok(view(fatcaId.value))
        case _                     => Ok(errorView(routes.IndexController.onPageLoad.url))
      }
  }

}
