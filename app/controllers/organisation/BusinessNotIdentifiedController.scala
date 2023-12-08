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

import config.FrontendAppConfig
import controllers.actions._
import models.ReporterType.{LimitedCompany, UnincorporatedAssociation}
import pages.ReporterTypePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.organisation.BusinessNotIdentifiedView

import javax.inject.Inject

class BusinessNotIdentifiedController @Inject() (
  override val messagesApi: MessagesApi,
  standardActionSets: StandardActionSets,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  view: BusinessNotIdentifiedView
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = standardActionSets.identifiedUserWithData() {
    implicit request =>
      val startUrl     = controllers.routes.IndexController.onPageLoad.url
      val reporterType = request.userAnswers.get(ReporterTypePage)

      val contactLink: String = request.userAnswers.get(ReporterTypePage) match {
        case Some(LimitedCompany) | Some(UnincorporatedAssociation) => appConfig.corporationTaxEnquiriesLink
        case _                                                      => appConfig.selfAssessmentEnquiriesLink
      }

      Ok(view(contactLink, reporterType, startUrl))
  }

}
