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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ContactDetailsMissingView

import javax.inject.Inject

object ContactDetailsMissingController {
  val continueUrlKey = "continueUrl"
}

class ContactDetailsMissingController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  view: ContactDetailsMissingView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = standardActionSets.subscriptionIdWithChangeDetailsRequired() {
    implicit request =>
      val continueUrl = request.flash.get(ContactDetailsMissingController.continueUrlKey).getOrElse(routes.IndexController.onPageLoad.url)
      Ok(view(continueUrl))
  }

}
