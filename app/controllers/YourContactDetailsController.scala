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
import models.Mode
import navigation.Navigator
import pages.YourContactDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.YourContactDetailsView

import javax.inject.Inject
import scala.concurrent.Future

class YourContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  navigator: Navigator,
  standardActionSets: StandardActionSets,
  checkForSubmission: CheckForSubmissionAction,
  val controllerComponents: MessagesControllerComponents,
  view: YourContactDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (standardActionSets.identifiedWithoutEnrolmentCheckInitialisedData() andThen checkForSubmission) {
    implicit request =>
      Ok(view(mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = standardActionSets.identifiedWithoutEnrolmentCheckInitialisedData().async {
    implicit request =>
      Future.successful(Redirect(navigator.nextPage(YourContactDetailsPage, mode, request.userAnswers)))
  }

}
